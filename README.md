# Agentic AI URL Shortener

The runnable Spring Boot application is in `urlshortener/urlshortener`.

## Run

```powershell
cd urlshortener\urlshortener
.\mvnw.cmd spring-boot:run
```

H2 console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:urlshortener`).

## Core API

```powershell
Invoke-RestMethod -Method Post http://localhost:8080/api/v1/urls -ContentType application/json -Body '{"url":"https://example.com"}'
Invoke-RestMethod http://localhost:8080/api/v1/urls/{code}
```

Requesting `http://localhost:8080/{code}` returns a `302` redirect and records an aggregate click.

## Agentic orchestration API

```powershell
Invoke-RestMethod -Method Post http://localhost:8080/api/v1/orchestration/runs -ContentType application/json -Body '{"requirement":"Add URL expiry"}'
```

The response provides persisted task nodes and explicit dependency edges. Complete, fail, re-plan, approve, safe-stop, and rollback endpoints are documented by the controller routes under `/api/v1/orchestration/runs`.

See the application [documentation](urlshortener/urlshortener/docs) for requirements, architecture, scenarios, and validation/risk controls.
