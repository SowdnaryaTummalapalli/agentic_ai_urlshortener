package agentic_ai.urlshortener.url.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import agentic_ai.urlshortener.url.domain.UrlMapping;
import agentic_ai.urlshortener.url.domain.UrlMappingRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DefaultUrlShorteningServiceTest {
    private final UrlMappingRepository repository = mock(UrlMappingRepository.class);
    private final ShortCodeGenerator generator = () -> "AbcD2345";
    private final DefaultUrlShorteningService service = new DefaultUrlShorteningService(repository, generator);

    @Test void creates_mapping_for_valid_https_url() {
        when(repository.existsByShortCode("AbcD2345")).thenReturn(false);
        when(repository.save(any(UrlMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UrlMapping result = service.shorten("https://example.com/path?q=x");
        assertEquals("AbcD2345", result.getShortCode());
        assertEquals("https://example.com/path?q=x", result.getTargetUrl());
    }

    @Test void rejects_non_http_schemes() { assertThrows(InvalidUrlException.class, () -> service.shorten("javascript:alert(1)")); }

    @Test void records_redirect_when_mapping_exists() {
        UrlMapping mapping = new UrlMapping("AbcD2345", "https://example.com");
        when(repository.findByShortCode("AbcD2345")).thenReturn(Optional.of(mapping));
        service.resolve("AbcD2345");
        assertEquals(1, mapping.getRedirectCount());
    }
}
