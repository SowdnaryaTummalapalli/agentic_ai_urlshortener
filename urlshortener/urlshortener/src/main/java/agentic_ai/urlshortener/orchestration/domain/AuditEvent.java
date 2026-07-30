package agentic_ai.urlshortener.orchestration.domain;

import jakarta.persistence.*;
import java.time.Instant;
@Entity
public class AuditEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private WorkflowRun run;
    @Column(nullable = false) private Instant occurredAt;
    @Column(nullable = false) private String eventType;
    @Column(nullable = false, length = 2000) private String detail;
    protected AuditEvent() { }
    public AuditEvent(WorkflowRun run, String eventType, String detail) { this.run = run; this.eventType = eventType; this.detail = detail; occurredAt = Instant.now(); }
    public Instant getOccurredAt() { return occurredAt; } public String getEventType() { return eventType; } public String getDetail() { return detail; }
}
