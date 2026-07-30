package agentic_ai.urlshortener.orchestration.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
public class WorkflowRun {
    @Id private UUID id;
    @Column(nullable = false, length = 4000) private String requirement;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private RunStatus status;
    @Column(nullable = false) private Instant startedAt;
    private Instant endedAt;
    @Column(nullable = false) private int retryCount;
    @Column(nullable = false) private int rollbackCount;
    @Column(nullable = false) private boolean humanApproved;
    protected WorkflowRun() { }
    public WorkflowRun(String requirement) { id = UUID.randomUUID(); this.requirement = requirement; status = RunStatus.RUNNING; startedAt = Instant.now(); }
    public UUID getId() { return id; } public String getRequirement() { return requirement; } public RunStatus getStatus() { return status; } public Instant getStartedAt() { return startedAt; } public Instant getEndedAt() { return endedAt; } public int getRetryCount() { return retryCount; } public int getRollbackCount() { return rollbackCount; } public boolean isHumanApproved() { return humanApproved; }
    public void approve() { humanApproved = true; if (status == RunStatus.WAITING_FOR_APPROVAL) status = RunStatus.RUNNING; }
    public void safeStop() { status = RunStatus.SAFE_STOPPED; endedAt = Instant.now(); }
    public void rollback() { rollbackCount++; status = RunStatus.ROLLED_BACK; endedAt = Instant.now(); }
    public void retry() { retryCount++; }
    public void waitForApproval() { status = RunStatus.WAITING_FOR_APPROVAL; }
    public void complete() { status = RunStatus.COMPLETED; endedAt = Instant.now(); }
}
