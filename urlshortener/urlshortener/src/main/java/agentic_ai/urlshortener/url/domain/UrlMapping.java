package agentic_ai.urlshortener.url.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.time.Instant;

/**
 * UrlMapping entity representing a short URL mapping.
 *
 * This entity stores the relationship between a short code and its target URL,
 * tracking access statistics and creation timestamp.
 *
 * @author Agentic AI Team
 * @version 1.0
 */
@Entity
public class UrlMapping {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 16)
    private String shortCode;

    @Column(nullable = false, length = 2048)
    private String targetUrl;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private long redirectCount;

    @Version
    private long version;

    /**
     * No-argument constructor for JPA.
     */
    protected UrlMapping() { }

    /**
     * Creates a new URL mapping with the specified short code and target URL.
     *
     * @param shortCode the unique short code identifier
     * @param targetUrl the target URL this mapping points to
     * @throws IllegalArgumentException if shortCode or targetUrl is null
     */
    public UrlMapping(String shortCode, String targetUrl) {
        if (shortCode == null || targetUrl == null) {
            throw new IllegalArgumentException("Short code and target URL cannot be null");
        }
        this.shortCode = shortCode;
        this.targetUrl = targetUrl;
        this.createdAt = Instant.now();
        this.redirectCount = 0;
    }

    /**
     * Records a redirect access for this URL mapping.
     * Increments the redirect count each time this method is called.
     */
    public void recordRedirect() {
        redirectCount++;
    }

    /**
     * Gets the unique short code.
     *
     * @return the short code
     */
    public String getShortCode() {
        return shortCode;
    }

    /**
     * Gets the target URL.
     *
     * @return the target URL this mapping points to
     */
    public String getTargetUrl() {
        return targetUrl;
    }

    /**
     * Gets the creation timestamp.
     *
     * @return the instant when this mapping was created
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the redirect count.
     *
     * @return the number of times this URL has been accessed
     */
    public long getRedirectCount() {
        return redirectCount;
    }
}
