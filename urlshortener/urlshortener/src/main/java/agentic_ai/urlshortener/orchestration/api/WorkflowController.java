package agentic_ai.urlshortener.orchestration.api;

import agentic_ai.urlshortener.orchestration.application.WorkflowService;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/orchestration/runs")
public class WorkflowController {
    private final WorkflowService service; public WorkflowController(WorkflowService service) { this.service = service; }
    record StartRequest(@NotBlank String requirement) { } record DetailRequest(@NotBlank String detail) { } record ApprovalRequest(@NotBlank String approver) { }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public WorkflowService.WorkflowView start(@RequestBody StartRequest request) { return service.view(service.start(request.requirement()).getId()); }
    @GetMapping("/{id}") public WorkflowService.WorkflowView view(@PathVariable UUID id) { return service.view(id); }
    @PostMapping("/{id}/approve") public WorkflowService.WorkflowView approve(@PathVariable UUID id, @RequestBody ApprovalRequest request) { service.approve(id, request.approver()); return service.view(id); }
    @PostMapping("/{id}/safe-stop") public WorkflowService.WorkflowView stop(@PathVariable UUID id, @RequestBody DetailRequest request) { service.safeStop(id, request.detail()); return service.view(id); }
    @PostMapping("/{id}/rollback") public WorkflowService.WorkflowView rollback(@PathVariable UUID id, @RequestBody DetailRequest request) { service.rollback(id, request.detail()); return service.view(id); }
    @PostMapping("/{id}/tasks/{taskId}/complete") public WorkflowService.WorkflowView complete(@PathVariable UUID id, @PathVariable Long taskId, @RequestBody DetailRequest request) { service.complete(id, taskId, request.detail()); return service.view(id); }
    @PostMapping("/{id}/tasks/{taskId}/fail") public WorkflowService.WorkflowView fail(@PathVariable UUID id, @PathVariable Long taskId, @RequestBody DetailRequest request) { service.fail(id, taskId, request.detail()); return service.view(id); }
    @PostMapping("/{id}/tasks/{taskId}/replan") public WorkflowService.WorkflowView replan(@PathVariable UUID id, @PathVariable Long taskId, @RequestBody DetailRequest request) { service.replan(id, taskId, request.detail()); return service.view(id); }
}
