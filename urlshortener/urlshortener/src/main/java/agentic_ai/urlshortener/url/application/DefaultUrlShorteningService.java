package agentic_ai.urlshortener.url.application;

import agentic_ai.urlshortener.url.domain.UrlMapping;
import agentic_ai.urlshortener.url.domain.UrlMappingRepository;
import java.net.URI;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of URL shortening service.
 *
 * Handles URL shortening, validation, and statistics tracking.
 * Provides transactional support for consistency.
 *
 * @author Agentic AI Team
 * @version 1.0
 */
@Service
@Transactional
public class DefaultUrlShorteningService implements UrlShorteningService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultUrlShorteningService.class);
    private static final int MAX_GENERATION_ATTEMPTS = 5;

    private final UrlMappingRepository repository;
    private final ShortCodeGenerator generator;

    /**
     * Constructs the service with required dependencies.
     *
     * @param repository the URL mapping repository
     * @param generator the short code generator
     */
    public DefaultUrlShorteningService(UrlMappingRepository repository, ShortCodeGenerator generator) {
        this.repository = repository;
        this.generator = generator;
    }

    /**
     * Shortens a URL by generating a unique short code.
     *
     * Validates the input URL format, generates a unique short code,
     * and persists the mapping to the repository.
     *
     * @param targetUrl the URL to shorten
     * @return the UrlMapping with assigned short code
     * @throws InvalidUrlException if URL format is invalid
     * @throws IllegalStateException if unable to generate unique code after retries
     */
    public UrlMapping shorten(String targetUrl) {
        logger.debug("Attempting to shorten URL: {}", targetUrl);
        validate(targetUrl);

        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            try {
                String code = generator.next();
                if (!repository.existsByShortCode(code)) {
                    UrlMapping mapping = new UrlMapping(code, targetUrl);
                    UrlMapping saved = repository.save(mapping);
                    logger.info("Successfully created short URL: {} -> {}", code, targetUrl);
                    return saved;
                }
                logger.debug("Short code collision, attempt {} of {}", attempt + 1, MAX_GENERATION_ATTEMPTS);
            } catch (Exception e) {
                logger.error("Error during code generation attempt {}", attempt + 1, e);
                if (attempt == MAX_GENERATION_ATTEMPTS - 1) {
                    throw e;
                }
            }
        }

        logger.error("Failed to allocate unique short code after {} attempts", MAX_GENERATION_ATTEMPTS);
        throw new IllegalStateException("Unable to allocate a unique short code; retry later");
    }

    /**
     * Resolves a short code to its target URL and records the access.
     *
     * @param shortCode the short code to resolve
     * @return the UrlMapping with incremented click count
     * @throws UrlNotFoundException if the short code is not found
     */
    public UrlMapping resolve(String shortCode) {
        logger.debug("Resolving short code: {}", shortCode);
        UrlMapping mapping = find(shortCode);
        mapping.recordRedirect();
        logger.info("Recorded redirect for short code: {}", shortCode);
        return mapping;
    }

    /**
     * Gets statistics for a short URL.
     *
     * @param shortCode the short code
     * @return the UrlMapping with statistics
     * @throws UrlNotFoundException if the short code is not found
     */
    @Transactional(readOnly = true)
    public UrlMapping statistics(String shortCode) {
        logger.debug("Retrieving statistics for short code: {}", shortCode);
        return find(shortCode);
    }

    /**
     * Internal method to find a URL mapping by short code.
     *
     * @param code the short code
     * @return the UrlMapping
     * @throws UrlNotFoundException if not found
     */
    private UrlMapping find(String code) {
        return repository.findByShortCode(code)
                .orElseThrow(() -> {
                    logger.warn("Short code not found: {}", code);
                    return new UrlNotFoundException(code);
                });
    }

    /**
     * Validates that the URL is a valid HTTP or HTTPS URL.
     *
     * @param value the URL to validate
     * @throws InvalidUrlException if URL is invalid
     */
    private void validate(String value) {
        if (value == null || value.trim().isEmpty()) {
            logger.warn("Validation failed: URL is null or empty");
            throw new InvalidUrlException();
        }

        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);

            if (!uri.isAbsolute() || uri.getHost() == null ||
                    !(scheme.equals("http") || scheme.equals("https"))) {
                logger.warn("Validation failed: Invalid URL - {}", value);
                throw new InvalidUrlException();
            }
        }
        catch (IllegalArgumentException exception) {
            logger.warn("Validation failed: Malformed URL - {}", value);
            throw new InvalidUrlException();
        }
    }
}
