package agentic_ai.urlshortener.orchestration.domain;

import jakarta.persistence.*;
@Entity
public class WorkflowEdge {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private WorkflowRun run;
    @ManyToOne(optional = false) private WorkflowTask predecessor;
    @ManyToOne(optional = false) private WorkflowTask successor;
    protected WorkflowEdge() { }
    public WorkflowEdge(WorkflowRun run, WorkflowTask predecessor, WorkflowTask successor) { this.run = run; this.predecessor = predecessor; this.successor = successor; }
    public WorkflowTask getPredecessor() { return predecessor; } public WorkflowTask getSuccessor() { return successor; }
}
