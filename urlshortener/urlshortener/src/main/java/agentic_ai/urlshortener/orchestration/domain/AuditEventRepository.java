package agentic_ai.urlshortener.orchestration.domain;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> { List<AuditEvent> findByRunIdOrderByOccurredAtAsc(UUID runId); }
