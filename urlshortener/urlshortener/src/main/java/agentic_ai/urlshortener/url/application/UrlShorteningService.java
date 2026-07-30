package agentic_ai.urlshortener.url.application;

import agentic_ai.urlshortener.url.domain.UrlMapping;

public interface UrlShorteningService {
    UrlMapping shorten(String targetUrl);
    UrlMapping resolve(String shortCode);
    UrlMapping statistics(String shortCode);
}
