package agentic_ai.urlshortener.url.api;

import agentic_ai.urlshortener.url.domain.UrlMapping;
import java.time.Instant;
public record ShortUrlResponse(String code, String shortUrl, String targetUrl, Instant createdAt) {
    static ShortUrlResponse from(UrlMapping mapping, String baseUrl) { return new ShortUrlResponse(mapping.getShortCode(), baseUrl + "/" + mapping.getShortCode(), mapping.getTargetUrl(), mapping.getCreatedAt()); }
}
