package agentic_ai.urlshortener.orchestration.domain;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface WorkflowEdgeRepository extends JpaRepository<WorkflowEdge, Long> { List<WorkflowEdge> findByRunId(UUID runId); List<WorkflowEdge> findBySuccessorId(Long taskId); List<WorkflowEdge> findByPredecessorId(Long taskId); }
