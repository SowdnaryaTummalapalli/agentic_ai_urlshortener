package agentic_ai.urlshortener.url.infrastructure;

import agentic_ai.urlshortener.url.application.ShortCodeGenerator;
import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class SecureShortCodeGenerator implements ShortCodeGenerator {
    private static final char[] ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private final SecureRandom random = new SecureRandom();
    public String next() { StringBuilder value = new StringBuilder(8); for (int i = 0; i < 8; i++) value.append(ALPHABET[random.nextInt(ALPHABET.length)]); return value.toString(); }
}
