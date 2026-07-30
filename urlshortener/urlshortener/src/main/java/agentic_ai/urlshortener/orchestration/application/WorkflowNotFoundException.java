package agentic_ai.urlshortener.orchestration.application;
import java.util.UUID; public class WorkflowNotFoundException extends RuntimeException { public WorkflowNotFoundException(UUID id) { super("Workflow run not found: " + id); } }
