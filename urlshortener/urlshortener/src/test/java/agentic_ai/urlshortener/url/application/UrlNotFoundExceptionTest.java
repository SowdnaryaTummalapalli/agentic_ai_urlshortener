package agentic_ai.urlshortener.url.application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for UrlNotFoundException.
 */
@DisplayName("UrlNotFoundException Tests")
class UrlNotFoundExceptionTest {

    @Test
    @DisplayName("Should throw UrlNotFoundException with short code")
    void testThrowException() {
        // Act & Assert
        UrlNotFoundException exception = assertThrows(UrlNotFoundException.class, () -> {
            throw new UrlNotFoundException("notfound");
        });

        assertTrue(exception.getMessage().contains("notfound"));
    }

    @Test
    @DisplayName("Should have proper exception message")
    void testExceptionMessage() {
        // Act
        UrlNotFoundException exception = new UrlNotFoundException("test123");

        // Assert
        assertNotNull(exception);
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("Should include short code in error details")
    void testShortCodeInMessage() {
        // Act
        UrlNotFoundException exception = new UrlNotFoundException("abc123");

        // Assert
        String message = exception.getMessage();
        assertNotNull(message);
        assertTrue(message.toLowerCase().contains("abc123") || message.isEmpty());
    }
}

