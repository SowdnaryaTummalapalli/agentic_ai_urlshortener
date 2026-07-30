package agentic_ai.urlshortener.orchestration.application;

import agentic_ai.urlshortener.orchestration.domain.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WorkflowService {
    private final WorkflowRunRepository runs; private final WorkflowTaskRepository tasks; private final WorkflowEdgeRepository edges; private final AuditEventRepository events; private final int maxRetries;
    public WorkflowService(WorkflowRunRepository runs, WorkflowTaskRepository tasks, WorkflowEdgeRepository edges, AuditEventRepository events, @Value("${orchestration.max-retries:3}") int maxRetries) { this.runs = runs; this.tasks = tasks; this.edges = edges; this.events = events; this.maxRetries = maxRetries; }
    public WorkflowRun start(String requirement) {
        WorkflowRun run = runs.save(new WorkflowRun(requirement));
        WorkflowTask requirements = tasks.save(new WorkflowTask(run, "Normalize requirement", "Requirement Agent", TaskStatus.READY));
        WorkflowTask design = tasks.save(new WorkflowTask(run, "Create architecture and design", "Architecture & Design Agent", TaskStatus.PENDING));
        WorkflowTask implementation = tasks.save(new WorkflowTask(run, "Implement approved change", "Implementation Agent", TaskStatus.PENDING));
        WorkflowTask validation = tasks.save(new WorkflowTask(run, "Validate risks and quality", "Validation & Risk Agent", TaskStatus.PENDING));
        WorkflowTask release = tasks.save(new WorkflowTask(run, "Prepare engineering summary and release readiness", "Orchestration Agent", TaskStatus.PENDING));
        edges.saveAll(List.of(new WorkflowEdge(run, requirements, design), new WorkflowEdge(run, design, implementation), new WorkflowEdge(run, design, validation), new WorkflowEdge(run, implementation, validation), new WorkflowEdge(run, validation, release)));
        event(run, "RUN_STARTED", "Dependency graph created with five agent tasks"); return run;
    }
    public WorkflowRun approve(UUID id, String approver) { WorkflowRun run = run(id); run.approve(); event(run, "HUMAN_APPROVAL", "Approved by " + approver); return run; }
    public WorkflowRun safeStop(UUID id, String reason) { WorkflowRun run = run(id); run.safeStop(); event(run, "SAFE_STOP", reason); return run; }
    public WorkflowRun rollback(UUID id, String reason) { WorkflowRun run = run(id); run.rollback(); event(run, "ROLLBACK", reason); return run; }
    public WorkflowTask complete(UUID runId, Long taskId, String output) {
        WorkflowRun run = run(runId); WorkflowTask task = task(taskId); ensureTaskBelongs(run, task);
        if (task.getName().startsWith("Prepare engineering summary") && !run.isHumanApproved()) {
            run.waitForApproval(); event(run, "APPROVAL_REQUIRED", "Human approval is required before release-readiness completion"); return task;
        }
        task.complete(output); event(run, "TASK_COMPLETED", task.getName() + " revision " + task.getRevision());
        for (WorkflowEdge edge : edges.findByPredecessorId(taskId)) if (allPredecessorsComplete(edge.getSuccessor().getId())) edge.getSuccessor().ready();
        if (task.getName().startsWith("Validate")) run.waitForApproval();
        if (allTasksComplete(runId)) run.complete();
        return task;
    }
    public WorkflowTask fail(UUID runId, Long taskId, String reason) {
        WorkflowRun run = run(runId); WorkflowTask task = task(taskId); ensureTaskBelongs(run, task); task.fail(); run.retry(); event(run, "TASK_FAILED", task.getName() + ": " + reason);
        if (run.getRetryCount() > maxRetries) { run.safeStop(); event(run, "RETRY_EXHAUSTED", "Safe-stop after " + maxRetries + " retries"); } else { task.ready(); event(run, "RETRY_SCHEDULED", "Attempt " + run.getRetryCount() + " of " + maxRetries); }
        return task;
    }
    public WorkflowTask replan(UUID runId, Long taskId, String rationale) {
        WorkflowRun run = run(runId); WorkflowTask task = task(taskId); ensureTaskBelongs(run, task); invalidateDownstream(task, new HashSet<>()); event(run, "REPLAN", rationale + "; downstream tasks invalidated"); return task;
    }
    @Transactional(readOnly = true) public WorkflowView view(UUID id) { WorkflowRun run = run(id); List<WorkflowTask> runTasks = tasks.findByRunId(id); return new WorkflowView(run, runTasks, edges.findByRunId(id), events.findByRunIdOrderByOccurredAtAsc(id), duration(run)); }
    private void invalidateDownstream(WorkflowTask task, Set<Long> visited) { if (!visited.add(task.getId())) return; for (WorkflowEdge edge : edges.findByPredecessorId(task.getId())) { WorkflowTask next = edge.getSuccessor(); next.invalidate(); invalidateDownstream(next, visited); } }
    private boolean allPredecessorsComplete(Long taskId) { return edges.findBySuccessorId(taskId).stream().allMatch(edge -> edge.getPredecessor().getStatus() == TaskStatus.COMPLETED); }
    private boolean allTasksComplete(UUID runId) { return tasks.findByRunId(runId).stream().allMatch(task -> task.getStatus() == TaskStatus.COMPLETED); }
    private WorkflowRun run(UUID id) { return runs.findById(id).orElseThrow(() -> new WorkflowNotFoundException(id)); }
    private WorkflowTask task(Long id) { return tasks.findById(id).orElseThrow(() -> new IllegalArgumentException("Workflow task not found: " + id)); }
    private void ensureTaskBelongs(WorkflowRun run, WorkflowTask task) { if (!tasks.findByRunId(run.getId()).stream().anyMatch(candidate -> candidate.getId().equals(task.getId()))) throw new IllegalArgumentException("Task does not belong to workflow run"); }
    private void event(WorkflowRun run, String type, String detail) { events.save(new AuditEvent(run, type, detail)); }
    private long duration(WorkflowRun run) { return Duration.between(run.getStartedAt(), run.getEndedAt() == null ? Instant.now() : run.getEndedAt()).toMillis(); }
    public record WorkflowView(WorkflowRun run, List<WorkflowTask> tasks, List<WorkflowEdge> edges, List<AuditEvent> auditEvents, long endToEndLatencyMillis) { }
}
