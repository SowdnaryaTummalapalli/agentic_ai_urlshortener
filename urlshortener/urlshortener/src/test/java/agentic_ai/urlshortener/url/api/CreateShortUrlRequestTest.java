package agentic_ai.urlshortener.url.api;

import static org.junit.jupiter.api.Assertions.*;

import agentic_ai.urlshortener.url.domain.UrlMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for CreateShortUrlRequest.
 */
@DisplayName("CreateShortUrlRequest Tests")
class CreateShortUrlRequestTest {

    private CreateShortUrlRequest request;

    @BeforeEach
    void setUp() {
        request = new CreateShortUrlRequest("https://example.com/test");
    }

    @Test
    @DisplayName("Should create request with valid URL")
    void testCreateValidRequest() {
        assertNotNull(request);
        assertEquals("https://example.com/test", request.url());
    }

    @Test
    @DisplayName("Should handle HTTPS URLs")
    void testHttpsUrl() {
        request = new CreateShortUrlRequest("https://secure.example.com");
        assertEquals("https://secure.example.com", request.url());
    }

    @Test
    @DisplayName("Should handle HTTP URLs")
    void testHttpUrl() {
        request = new CreateShortUrlRequest("http://example.com");
        assertEquals("http://example.com", request.url());
    }

    @Test
    @DisplayName("Should handle URLs with query parameters")
    void testUrlWithQueryParams() {
        request = new CreateShortUrlRequest("https://example.com/path?key=value&other=data");
        assertEquals("https://example.com/path?key=value&other=data", request.url());
    }
}

