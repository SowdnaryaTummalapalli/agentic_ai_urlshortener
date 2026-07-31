package agentic_ai.urlshortener.orchestration.application;

import agentic_ai.urlshortener.orchestration.domain.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing agent-based workflow orchestration.
 *
 * Coordinates multi-agent workflows with dependency management, task tracking,
 * approval workflows, and audit trail management.
 *
 * @author Agentic AI Team
 * @version 1.0
 */
@Service
@Transactional
public class WorkflowService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowService.class);

    private final WorkflowRunRepository runs;
    private final WorkflowTaskRepository tasks;
    private final WorkflowEdgeRepository edges;
    private final AuditEventRepository events;
    private final int maxRetries;

    /**
     * Constructs the workflow service.
     *
     * @param runs repository for workflow runs
     * @param tasks repository for workflow tasks
     * @param edges repository for task dependencies
     * @param events repository for audit events
     * @param maxRetries maximum number of retries before safe-stop (default 3)
     */
    public WorkflowService(WorkflowRunRepository runs, WorkflowTaskRepository tasks,
            WorkflowEdgeRepository edges, AuditEventRepository events,
            @Value("${orchestration.max-retries:3}") int maxRetries) {
        this.runs = runs;
        this.tasks = tasks;
        this.edges = edges;
        this.events = events;
        this.maxRetries = maxRetries;
    }

    /**
     * Starts a new workflow run with the given requirement.
     *
     * Creates a 5-task workflow with dependencies managed by the orchestration framework.
     * Tasks: Normalize Requirement -> Architecture & Design -> Implementation & Validation -> Release Readiness
     *
     * @param requirement the user's requirement for agentic processing
     * @return the created workflow run
     */
    public WorkflowRun start(String requirement) {
        logger.info("Starting workflow for requirement: {}", requirement);
        WorkflowRun run = runs.save(new WorkflowRun(requirement));

        // Create workflow tasks
        WorkflowTask requirementTask = tasks.save(new WorkflowTask(run, "Normalize requirement", "Requirement Agent", TaskStatus.READY));
        WorkflowTask designTask = tasks.save(new WorkflowTask(run, "Create architecture and design", "Architecture & Design Agent", TaskStatus.PENDING));
        WorkflowTask implementationTask = tasks.save(new WorkflowTask(run, "Implement approved change", "Implementation Agent", TaskStatus.PENDING));
        WorkflowTask validationTask = tasks.save(new WorkflowTask(run, "Validate risks and quality", "Validation & Risk Agent", TaskStatus.PENDING));
        WorkflowTask releaseTask = tasks.save(new WorkflowTask(run, "Prepare engineering summary and release readiness", "Orchestration Agent", TaskStatus.PENDING));

        // Create task dependencies
        edges.saveAll(List.of(
            new WorkflowEdge(run, requirementTask, designTask),
            new WorkflowEdge(run, designTask, implementationTask),
            new WorkflowEdge(run, designTask, validationTask),
            new WorkflowEdge(run, implementationTask, validationTask),
            new WorkflowEdge(run, validationTask, releaseTask)
        ));

        event(run, "RUN_STARTED", "Dependency graph created with five agent tasks");
        logger.debug("Workflow created with ID: {} and requirement: {}", run.getId(), requirement);
        return run;
    }

    /**
     * Approves a workflow run for progression.
     *
     * @param id the workflow run ID
     * @param approver the name/ID of the approver
     * @return the updated workflow run
     * @throws WorkflowNotFoundException if workflow not found
     */
    public WorkflowRun approve(UUID id, String approver) {
        logger.info("Approving workflow {} by approver: {}", id, approver);
        WorkflowRun run = run(id);
        run.approve();
        event(run, "HUMAN_APPROVAL", "Approved by " + approver);
        return run;
    }

    /**
     * Safely stops a workflow run.
     *
     * @param id the workflow run ID
     * @param reason the reason for stopping
     * @return the updated workflow run
     * @throws WorkflowNotFoundException if workflow not found
     */
    public WorkflowRun safeStop(UUID id, String reason) {
        logger.warn("Safe-stopping workflow {}: {}", id, reason);
        WorkflowRun run = run(id);
        run.safeStop();
        event(run, "SAFE_STOP", reason);
        return run;
    }

    /**
     * Rolls back a workflow run to its initial state.
     *
     * @param id the workflow run ID
     * @param reason the reason for rollback
     * @return the updated workflow run
     * @throws WorkflowNotFoundException if workflow not found
     */
    public WorkflowRun rollback(UUID id, String reason) {
        logger.warn("Rolling back workflow {}: {}", id, reason);
        WorkflowRun run = run(id);
        run.rollback();
        event(run, "ROLLBACK", reason);
        return run;
    }

    /**
     * Completes a workflow task with optional output.
     *
     * Updates task status, triggers downstream task readiness, and manages approval workflows.
     *
     * @param runId the workflow run ID
     * @param taskId the task ID to complete
     * @param output the task output/result
     * @return the updated task
     * @throws WorkflowNotFoundException if workflow not found
     */
    public WorkflowTask complete(UUID runId, Long taskId, String output) {
        logger.info("Completing task {} in workflow {}", taskId, runId);
        WorkflowRun run = run(runId);
        WorkflowTask task = task(taskId);
        ensureTaskBelongs(run, task);

        if (task.getName().startsWith("Prepare engineering summary") && !run.isHumanApproved()) {
            run.waitForApproval();
            event(run, "APPROVAL_REQUIRED", "Human approval is required before release-readiness completion");
            logger.debug("Workflow requires human approval before release");
            return task;
        }

        task.complete(output);
        event(run, "TASK_COMPLETED", task.getName() + " revision " + task.getRevision());

        // Trigger downstream tasks when predecessors complete
        for (WorkflowEdge edge : edges.findByPredecessorId(taskId)) {
            if (allPredecessorsComplete(edge.getSuccessor().getId())) {
                edge.getSuccessor().ready();
                logger.debug("Downstream task {} is ready to proceed", edge.getSuccessor().getId());
            }
        }

        if (task.getName().startsWith("Validate")) {
            run.waitForApproval();
            logger.debug("Validation task completed, awaiting approval");
        }

        if (allTasksComplete(runId)) {
            run.complete();
            logger.info("All tasks completed for workflow {}", runId);
        }

        return task;
    }

    /**
     * Marks a workflow task as failed and manages retry logic.
     *
     * @param runId the workflow run ID
     * @param taskId the task ID that failed
     * @param reason the failure reason
     * @return the updated task
     * @throws WorkflowNotFoundException if workflow not found
     */
    public WorkflowTask fail(UUID runId, Long taskId, String reason) {
        logger.error("Task {} in workflow {} failed: {}", taskId, runId, reason);
        WorkflowRun run = run(runId);
        WorkflowTask task = task(taskId);
        ensureTaskBelongs(run, task);
        task.fail();
        run.retry();
        event(run, "TASK_FAILED", task.getName() + ": " + reason);

        if (run.getRetryCount() > maxRetries) {
            run.safeStop();
            event(run, "RETRY_EXHAUSTED", "Safe-stop after " + maxRetries + " retries");
            logger.error("Retry limit exceeded for workflow {}, safe-stopping", runId);
        } else {
            task.ready();
            event(run, "RETRY_SCHEDULED", "Attempt " + run.getRetryCount() + " of " + maxRetries);
            logger.info("Scheduled retry {} of {} for task {} in workflow {}", run.getRetryCount(), maxRetries, taskId, runId);
        }
        return task;
    }

    /**
     * Replans a workflow task and invalidates all downstream tasks.
     *
     * @param runId the workflow run ID
     * @param taskId the task ID to replan
     * @param rationale the replan rationale
     * @return the updated task
     * @throws WorkflowNotFoundException if workflow not found
     */
    public WorkflowTask replan(UUID runId, Long taskId, String rationale) {
        logger.warn("Replanning task {} in workflow {}: {}", taskId, runId, rationale);
        WorkflowRun run = run(runId);
        WorkflowTask task = task(taskId);
        ensureTaskBelongs(run, task);
        invalidateDownstream(task, new HashSet<>());
        event(run, "REPLAN", rationale + "; downstream tasks invalidated");
        logger.debug("Downstream tasks invalidated after replan of task {}", taskId);
        return task;
    }

    /**
     * Retrieves the complete view of a workflow run.
     *
     * @param id the workflow run ID
     * @return a comprehensive view with run, tasks, edges, audit trail, and latency
     * @throws WorkflowNotFoundException if workflow not found
     */
    @Transactional(readOnly = true)
    public WorkflowView view(UUID id) {
        logger.debug("Retrieving view for workflow {}", id);
        WorkflowRun run = run(id);
        List<WorkflowTask> runTasks = tasks.findByRunId(id);
        return new WorkflowView(run, runTasks, edges.findByRunId(id),
            events.findByRunIdOrderByOccurredAtAsc(id), duration(run));
    }

    /**
     * Invalidates a task and all its downstream dependencies recursively.
     *
     * @param task the task to invalidate
     * @param visited set of already visited tasks to prevent cycles
     */
    private void invalidateDownstream(WorkflowTask task, Set<Long> visited) {
        if (!visited.add(task.getId())) return;
        for (WorkflowEdge edge : edges.findByPredecessorId(task.getId())) {
            WorkflowTask next = edge.getSuccessor();
            next.invalidate();
            invalidateDownstream(next, visited);
        }
    }

    /**
     * Checks if all predecessor tasks for a given task are complete.
     *
     * @param taskId the task ID to check
     * @return true if all predecessors are completed
     */
    private boolean allPredecessorsComplete(Long taskId) {
        return edges.findBySuccessorId(taskId).stream()
            .allMatch(edge -> edge.getPredecessor().getStatus() == TaskStatus.COMPLETED);
    }

    /**
     * Checks if all tasks in a workflow are complete.
     *
     * @param runId the workflow run ID
     * @return true if all tasks are completed
     */
    private boolean allTasksComplete(UUID runId) {
        return tasks.findByRunId(runId).stream()
            .allMatch(task -> task.getStatus() == TaskStatus.COMPLETED);
    }

    /**
     * Retrieves a workflow run by ID.
     *
     * @param id the workflow run ID
     * @return the workflow run
     * @throws WorkflowNotFoundException if not found
     */
    private WorkflowRun run(UUID id) {
        return runs.findById(id).orElseThrow(() -> {
            logger.warn("Workflow not found: {}", id);
            return new WorkflowNotFoundException(id);
        });
    }

    /**
     * Retrieves a workflow task by ID.
     *
     * @param id the task ID
     * @return the workflow task
     * @throws IllegalArgumentException if not found
     */
    private WorkflowTask task(Long id) {
        return tasks.findById(id).orElseThrow(() -> {
            logger.warn("Workflow task not found: {}", id);
            return new IllegalArgumentException("Workflow task not found: " + id);
        });
    }

    /**
     * Validates that a task belongs to a workflow run.
     *
     * @param run the workflow run
     * @param task the task to validate
     * @throws IllegalArgumentException if task doesn't belong to run
     */
    private void ensureTaskBelongs(WorkflowRun run, WorkflowTask task) {
        if (!tasks.findByRunId(run.getId()).stream()
                .anyMatch(candidate -> candidate.getId().equals(task.getId()))) {
            logger.error("Task {} does not belong to workflow {}", task.getId(), run.getId());
            throw new IllegalArgumentException("Task does not belong to workflow run");
        }
    }

    /**
     * Records an audit event for a workflow.
     *
     * @param run the workflow run
     * @param type the event type
     * @param detail the event detail
     */
    private void event(WorkflowRun run, String type, String detail) {
        events.save(new AuditEvent(run, type, detail));
        logger.debug("Audit event recorded for workflow {}: {} - {}", run.getId(), type, detail);
    }

    /**
     * Calculates the duration of a workflow run.
     *
     * @param run the workflow run
     * @return the duration in milliseconds
     */
    private long duration(WorkflowRun run) {
        return Duration.between(run.getStartedAt(),
            run.getEndedAt() == null ? Instant.now() : run.getEndedAt()).toMillis();
    }

    /**
     * Comprehensive view of a workflow run including all related entities and audit trail.
     */
    public record WorkflowView(WorkflowRun run, List<WorkflowTask> tasks, List<WorkflowEdge> edges,
            List<AuditEvent> auditEvents, long endToEndLatencyMillis) { }
}
