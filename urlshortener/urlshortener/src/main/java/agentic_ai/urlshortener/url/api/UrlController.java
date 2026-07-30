package agentic_ai.urlshortener.url.api;

import agentic_ai.urlshortener.url.application.UrlShorteningService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/urls")
public class UrlController {
    private final UrlShorteningService service;
    private final String baseUrl;
    public UrlController(UrlShorteningService service, @Value("${url-shortener.base-url}") String baseUrl) { this.service = service; this.baseUrl = baseUrl.replaceAll("/$", ""); }
    @PostMapping public ResponseEntity<ShortUrlResponse> shorten(@Valid @RequestBody CreateShortUrlRequest request) { var mapping = service.shorten(request.url()); return ResponseEntity.created(URI.create(baseUrl + "/" + mapping.getShortCode())).body(ShortUrlResponse.from(mapping, baseUrl)); }
    @GetMapping("/{code}") public UrlStatisticsResponse statistics(@PathVariable String code) { return UrlStatisticsResponse.from(service.statistics(code)); }
}
