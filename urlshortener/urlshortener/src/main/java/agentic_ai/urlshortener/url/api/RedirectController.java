package agentic_ai.urlshortener.url.api;

import agentic_ai.urlshortener.url.application.UrlShorteningService;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedirectController {
    private final UrlShorteningService service;
    public RedirectController(UrlShorteningService service) { this.service = service; }
    @GetMapping("/{code:[a-zA-Z0-9]{8}}") public ResponseEntity<Void> redirect(@PathVariable String code) { return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(service.resolve(code).getTargetUrl())).build(); }
}
