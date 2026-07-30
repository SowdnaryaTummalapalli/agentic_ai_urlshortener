package agentic_ai.urlshortener.url.application;

import agentic_ai.urlshortener.url.domain.UrlMapping;
import agentic_ai.urlshortener.url.domain.UrlMappingRepository;
import java.net.URI;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DefaultUrlShorteningService implements UrlShorteningService {
    private static final int MAX_GENERATION_ATTEMPTS = 5;
    private final UrlMappingRepository repository;
    private final ShortCodeGenerator generator;
    public DefaultUrlShorteningService(UrlMappingRepository repository, ShortCodeGenerator generator) { this.repository = repository; this.generator = generator; }
    public UrlMapping shorten(String targetUrl) {
        validate(targetUrl);
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String code = generator.next();
            if (!repository.existsByShortCode(code)) return repository.save(new UrlMapping(code, targetUrl));
        }
        throw new IllegalStateException("Unable to allocate a unique short code; retry later");
    }
    public UrlMapping resolve(String shortCode) { UrlMapping mapping = find(shortCode); mapping.recordRedirect(); return mapping; }
    @Transactional(readOnly = true) public UrlMapping statistics(String shortCode) { return find(shortCode); }
    private UrlMapping find(String code) { return repository.findByShortCode(code).orElseThrow(() -> new UrlNotFoundException(code)); }
    private void validate(String value) {
        try { URI uri = URI.create(value); String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT); if (!uri.isAbsolute() || uri.getHost() == null || !(scheme.equals("http") || scheme.equals("https"))) throw new InvalidUrlException(); }
        catch (IllegalArgumentException exception) { throw new InvalidUrlException(); }
    }
}
