package agentic_ai.urlshortener.url.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.time.Instant;

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
    @Version private long version;

    protected UrlMapping() { }
    public UrlMapping(String shortCode, String targetUrl) { this.shortCode = shortCode; this.targetUrl = targetUrl; this.createdAt = Instant.now(); }
    public void recordRedirect() { redirectCount++; }
    public String getShortCode() { return shortCode; }
    public String getTargetUrl() { return targetUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public long getRedirectCount() { return redirectCount; }
}
