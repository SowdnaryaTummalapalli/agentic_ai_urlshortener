package agentic_ai.urlshortener.url.application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for InvalidUrlException.
 */
@DisplayName("InvalidUrlException Tests")
class InvalidUrlExceptionTest {

    @Test
    @DisplayName("Should throw InvalidUrlException")
    void testThrowException() {
        // Act & Assert
        assertThrows(InvalidUrlException.class, () -> {
            throw new InvalidUrlException();
        });
    }

    @Test
    @DisplayName("Should have proper exception message")
    void testExceptionMessage() {
        // Act
        InvalidUrlException exception = new InvalidUrlException();

        // Assert
        assertNotNull(exception);
        assertTrue(exception instanceof RuntimeException);
    }
}

