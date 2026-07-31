package agentic_ai.urlshortener.common;

import agentic_ai.urlshortener.url.application.InvalidUrlException;
import agentic_ai.urlshortener.url.application.UrlNotFoundException;
import agentic_ai.urlshortener.orchestration.application.WorkflowNotFoundException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for REST API.
 *
 * Handles various application exceptions and returns appropriate HTTP status codes
 * and error response messages.
 *
 * @author Agentic AI Team
 * @version 1.0
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /**
     * Standard error response format.
     */
    record ErrorResponse(Instant timestamp, int status, String code, String message) { }

    /**
     * Handles resource not found exceptions.
     *
     * @param e the exception
     * @return error response with 404 status
     */
    @ExceptionHandler({UrlNotFoundException.class, WorkflowNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse missing(RuntimeException e) {
        logger.warn("Resource not found: {}", e.getMessage());
        return error(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", e.getMessage());
    }

    /**
     * Handles invalid input exceptions.
     *
     * @param e the exception
     * @return error response with 400 status
     */
    @ExceptionHandler({InvalidUrlException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse invalid(Exception e) {
        logger.warn("Invalid input: {}", e.getMessage());
        return error(HttpStatus.BAD_REQUEST, "INVALID_URL", "A valid HTTP or HTTPS URL is required");
    }

    /**
     * Handles service unavailable exceptions.
     *
     * @param e the exception
     * @return error response with 503 status
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    ErrorResponse unavailable(IllegalStateException e) {
        logger.error("Service temporarily unavailable: {}", e.getMessage());
        return error(HttpStatus.SERVICE_UNAVAILABLE, "CAPACITY_UNAVAILABLE", e.getMessage());
    }

    /**
     * Creates a standardized error response.
     *
     * @param status the HTTP status
     * @param code the error code
     * @param message the error message
     * @return the error response
     */
    private ErrorResponse error(HttpStatus status, String code, String message) {
        return new ErrorResponse(Instant.now(), status.value(), code, message);
    }
}
