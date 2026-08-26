package md.ramaiana.foodmarket.shared.util;

import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Generates and hashes single-use secure tokens (e.g. registration confirmation links).
 * Only the hash of a generated token is ever persisted — the raw token exists only in memory
 * and in the link handed to the user, so a database leak does not hand out working links.
 */
@Component
public class SecureTokenGenerator {

    private static final int TOKEN_BYTE_LENGTH = 32;
    private static final String HASH_ALGORITHM = "SHA-256";

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a new random, URL-safe token.
     */
    @NonNull
    public String generate() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Hash a raw token for storage/lookup. Deterministic: the same input always produces the same hash.
     */
    @NonNull
    public String hash(@NonNull String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available on every JVM (JLS-mandated algorithm)
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
