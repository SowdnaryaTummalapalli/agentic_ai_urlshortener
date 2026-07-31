package agentic_ai.urlshortener.url.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for SecureShortCodeGenerator.
 * Tests the cryptographic short code generation.
 */
@DisplayName("SecureShortCodeGenerator Tests")
class SecureShortCodeGeneratorTest {

    private SecureShortCodeGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new SecureShortCodeGenerator();
    }

    @Test
    @DisplayName("Should generate non-null short codes")
    void testGenerateNonNull() {
        // Act
        String code = generator.next();

        // Assert
        assertNotNull(code);
        assertFalse(code.isEmpty());
    }

    @Test
    @DisplayName("Should generate unique short codes")
    void testGenerateUnique() {
        // Act
        String code1 = generator.next();
        String code2 = generator.next();
        String code3 = generator.next();

        // Assert
        assertNotEquals(code1, code2);
        assertNotEquals(code2, code3);
        assertNotEquals(code1, code3);
    }

    @Test
    @DisplayName("Should generate codes with consistent length")
    void testGenerateConsistentLength() {
        // Act
        String code1 = generator.next();
        String code2 = generator.next();
        String code3 = generator.next();

        // Assert
        assertEquals(code1.length(), code2.length());
        assertEquals(code2.length(), code3.length());
    }

    @Test
    @DisplayName("Should generate URL-safe codes")
    void testGenerateUrlSafe() {
        // Act
        String code = generator.next();

        // Assert
        assertTrue(code.matches("[A-Za-z0-9_-]*"));
    }
}

