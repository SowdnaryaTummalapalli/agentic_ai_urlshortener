package agentic_ai.urlshortener.url.application;
public class InvalidUrlException extends RuntimeException { public InvalidUrlException() { super("Only absolute HTTP or HTTPS URLs are allowed"); } }
