package agentic_ai.urlshortener.url.api;

import agentic_ai.urlshortener.url.domain.UrlMapping;
import java.time.Instant;
public record UrlStatisticsResponse(String code, String targetUrl, long redirectCount, Instant createdAt) {
    static UrlStatisticsResponse from(UrlMapping mapping) { return new UrlStatisticsResponse(mapping.getShortCode(), mapping.getTargetUrl(), mapping.getRedirectCount(), mapping.getCreatedAt()); }
}
