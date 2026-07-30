package agentic_ai.urlshortener.orchestration.domain;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface WorkflowTaskRepository extends JpaRepository<WorkflowTask, Long> { List<WorkflowTask> findByRunId(UUID runId); }
