package agentic_ai.urlshortener.orchestration.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class WorkflowTask {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private WorkflowRun run;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String agent;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private TaskStatus status;
    @Column(length = 4000) private String output;
    @Column(nullable = false) private int revision;
    @Column(nullable = false) private Instant updatedAt;
    protected WorkflowTask() { }
    public WorkflowTask(WorkflowRun run, String name, String agent, TaskStatus status) { this.run = run; this.name = name; this.agent = agent; this.status = status; updatedAt = Instant.now(); }
    public Long getId() { return id; } public String getName() { return name; } public String getAgent() { return agent; } public TaskStatus getStatus() { return status; } public String getOutput() { return output; } public int getRevision() { return revision; }
    public void ready() { status = TaskStatus.READY; updatedAt = Instant.now(); }
    public void complete(String output) { status = TaskStatus.COMPLETED; this.output = output; revision++; updatedAt = Instant.now(); }
    public void fail() { status = TaskStatus.FAILED; updatedAt = Instant.now(); }
    public void invalidate() { status = TaskStatus.PENDING; revision++; updatedAt = Instant.now(); }
}
