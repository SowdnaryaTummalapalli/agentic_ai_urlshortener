package agentic_ai.urlshortener.orchestration.api;

import agentic_ai.urlshortener.orchestration.application.WorkflowService;
import agentic_ai.urlshortener.orchestration.domain.WorkflowRun;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for workflow orchestration operations.
 *
 * Provides endpoints for managing agent-based workflow executions including:
 * - Starting new workflow runs
 * - Viewing workflow status
 * - Approving changes
 * - Completing/failing tasks
 * - Safe-stopping and rolling back workflows
 *
 * @author Agentic AI Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/orchestration/runs")
public class WorkflowController {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowController.class);

    private final WorkflowService service;

    /**
     * Constructs a new WorkflowController.
     *
     * @param service the workflow orchestration service
     */
    public WorkflowController(WorkflowService service) {
        this.service = service;
    }

    /**
     * Request body for starting a workflow run.
     */
    record StartRequest(@NotBlank String requirement) { }

    /**
     * Request body for workflow detail operations.
     */
    record DetailRequest(@NotBlank String detail) { }

    /**
     * Request body for approval operations.
     */
    record ApprovalRequest(@NotBlank String approver) { }

    /**
     * Starts a new workflow run with the given requirement.
     *
     * @param request contains the requirement to process
     * @return the workflow view with initial state
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkflowService.WorkflowView start(@RequestBody StartRequest request) {
        logger.info("Starting workflow for requirement: {}", request.requirement());
        WorkflowRun run = service.start(request.requirement());
        logger.debug("Created workflow run with ID: {}", run.getId());
        return service.view(run.getId());
    }

    /**
     * Retrieves a workflow run by its ID.
     *
     * @param id the workflow run ID
     * @return the workflow view with current state
     */
    @GetMapping("/{id}")
    public WorkflowService.WorkflowView view(@PathVariable UUID id) {
        logger.debug("Viewing workflow: {}", id);
        return service.view(id);
    }

    /**
     * Approves a workflow run.
     *
     * @param id the workflow run ID
     * @param request contains the approver information
     * @return the updated workflow view
     */
    @PostMapping("/{id}/approve")
    public WorkflowService.WorkflowView approve(@PathVariable UUID id, @RequestBody ApprovalRequest request) {
        logger.info("Approving workflow {} by {}", id, request.approver());
        service.approve(id, request.approver());
        return service.view(id);
    }

    /**
     * Safely stops a workflow run.
     *
     * @param id the workflow run ID
     * @param request contains the reason for stopping
     * @return the updated workflow view
     */
    @PostMapping("/{id}/safe-stop")
    public WorkflowService.WorkflowView stop(@PathVariable UUID id, @RequestBody DetailRequest request) {
        logger.info("Safe-stopping workflow {}: {}", id, request.detail());
        service.safeStop(id, request.detail());
        return service.view(id);
    }

    /**
     * Rolls back a workflow run.
     *
     * @param id the workflow run ID
     * @param request contains the rollback reason
     * @return the updated workflow view
     */
    @PostMapping("/{id}/rollback")
    public WorkflowService.WorkflowView rollback(@PathVariable UUID id, @RequestBody DetailRequest request) {
        logger.info("Rolling back workflow {}: {}", id, request.detail());
        service.rollback(id, request.detail());
        return service.view(id);
    }

    /**
     * Completes a workflow task.
     *
     * @param id the workflow run ID
     * @param taskId the task ID
     * @param request contains the task output
     * @return the updated workflow view
     */
    @PostMapping("/{id}/tasks/{taskId}/complete")
    public WorkflowService.WorkflowView complete(@PathVariable UUID id, @PathVariable Long taskId, @RequestBody DetailRequest request) {
        logger.info("Completing task {} in workflow {}", taskId, id);
        service.complete(id, taskId, request.detail());
        return service.view(id);
    }

    /**
     * Marks a workflow task as failed.
     *
     * @param id the workflow run ID
     * @param taskId the task ID
     * @param request contains the failure reason
     * @return the updated workflow view
     */
    @PostMapping("/{id}/tasks/{taskId}/fail")
    public WorkflowService.WorkflowView fail(@PathVariable UUID id, @PathVariable Long taskId, @RequestBody DetailRequest request) {
        logger.warn("Failing task {} in workflow {}: {}", taskId, id, request.detail());
        service.fail(id, taskId, request.detail());
        return service.view(id);
    }

    /**
     * Replans a workflow task and invalidates downstream tasks.
     *
     * @param id the workflow run ID
     * @param taskId the task ID to replan
     * @param request contains the replan rationale
     * @return the updated workflow view
     */
    @PostMapping("/{id}/tasks/{taskId}/replan")
    public WorkflowService.WorkflowView replan(@PathVariable UUID id, @PathVariable Long taskId, @RequestBody DetailRequest request) {
        logger.info("Replanning task {} in workflow {}: {}", taskId, id, request.detail());
        service.replan(id, taskId, request.detail());
        return service.view(id);
    }
}
