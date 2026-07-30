package agentic_ai.urlshortener.common;

import agentic_ai.urlshortener.url.application.InvalidUrlException;
import agentic_ai.urlshortener.url.application.UrlNotFoundException;
import agentic_ai.urlshortener.orchestration.application.WorkflowNotFoundException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    record ErrorResponse(Instant timestamp, int status, String code, String message) { }
    @ExceptionHandler({UrlNotFoundException.class, WorkflowNotFoundException.class}) @ResponseStatus(HttpStatus.NOT_FOUND) ErrorResponse missing(RuntimeException e) { return error(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", e.getMessage()); }
    @ExceptionHandler({InvalidUrlException.class, MethodArgumentNotValidException.class}) @ResponseStatus(HttpStatus.BAD_REQUEST) ErrorResponse invalid(Exception e) { return error(HttpStatus.BAD_REQUEST, "INVALID_URL", "A valid HTTP or HTTPS URL is required"); }
    @ExceptionHandler(IllegalStateException.class) @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE) ErrorResponse unavailable(IllegalStateException e) { return error(HttpStatus.SERVICE_UNAVAILABLE, "CAPACITY_UNAVAILABLE", e.getMessage()); }
    private ErrorResponse error(HttpStatus status, String code, String message) { return new ErrorResponse(Instant.now(), status.value(), code, message); }
}
