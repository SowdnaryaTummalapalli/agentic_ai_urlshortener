# Agentic scenarios

## Greenfield: URL shortener

The Requirement Agent baselines create/redirect/statistics behaviors. The Architecture Agent creates the module boundaries. Implementation and validation run as governed tasks; validation waits for both architecture and implementation. A human approves release readiness.

## Brownfield: add expiry to shortened URLs

The Architecture Agent identifies the affected mapping entity, API DTOs, resolution rule, database migration, tests, and documentation. The Orchestration Agent invalidates validation and release nodes when the entity contract changes, then schedules the new dependency graph. Migration/release requires human approval.

## Ambiguous: “add analytics”

The Requirement Agent records the ambiguity: aggregate clicks versus visitor-level tracking, retention, and privacy. It selects aggregate-only clicks as the safe prototype default and raises any user-level tracking for human approval. The Validation & Risk Agent verifies that no personal data reaches audit or analytics storage.
