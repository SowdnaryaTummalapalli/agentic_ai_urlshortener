package agentic_ai.urlshortener.url.api;

import agentic_ai.urlshortener.url.application.UrlShorteningService;
import jakarta.validation.Valid;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for URL shortening operations.
 *
 * Provides endpoints for:
 * - Creating short URLs from long URLs
 * - Retrieving statistics about short URLs
 *
 * @author Agentic AI Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/urls")
public class UrlController {

    private static final Logger logger = LoggerFactory.getLogger(UrlController.class);

    private final UrlShorteningService service;
    private final String baseUrl;

    /**
     * Constructs a new UrlController.
     *
     * @param service the URL shortening service
     * @param baseUrl the base URL for shortened links (configurable)
     */
    public UrlController(UrlShorteningService service, @Value("${url-shortener.base-url}") String baseUrl) {
        this.service = service;
        this.baseUrl = baseUrl.replaceAll("/$", "");
    }

    /**
     * Creates a short URL from a long URL.
     *
     * @param request the request containing the URL to shorten
     * @return a response with the generated short code and full short URL
     */
    @PostMapping
    public ResponseEntity<ShortUrlResponse> shorten(@Valid @RequestBody CreateShortUrlRequest request) {
        try {
            logger.debug("Shortening URL: {}", request.url());
            var mapping = service.shorten(request.url());
            logger.info("Successfully shortened URL with code: {}", mapping.getShortCode());
            return ResponseEntity.created(URI.create(baseUrl + "/" + mapping.getShortCode()))
                    .body(ShortUrlResponse.from(mapping, baseUrl));
        } catch (Exception e) {
            logger.error("Error shortening URL", e);
            throw e;
        }
    }

    /**
     * Retrieves statistics for a short URL.
     *
     * @param code the short code
     * @return statistics including redirect count and creation time
     */
    @GetMapping("/{code}")
    public UrlStatisticsResponse statistics(@PathVariable String code) {
        try {
            logger.debug("Retrieving statistics for code: {}", code);
            return UrlStatisticsResponse.from(service.statistics(code));
        } catch (Exception e) {
            logger.error("Error retrieving statistics for code: {}", code, e);
            throw e;
        }
    }
}
