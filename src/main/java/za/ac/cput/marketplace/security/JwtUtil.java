package za.ac.cput.marketplace.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // This secret key is used to "sign" tokens so we can trust they weren't tampered with.
    // In a real production app, this would come from a secure config file, not hardcoded.
    private final SecretKey key = Keys.hmacShaKeyFor(
            "this-is-a-super-secret-key-for-student-marketplace-2026".getBytes()
    );

    private final long expirationMs = 1000 * 60 * 60 * 10; // 10 hours

    // Create a token containing the user's email, database id, and their current mode (BUYER/SELLER/ADMIN)
    public String generateToken(String email, Long userId, String role, String loginAs) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .claim("loginAs", loginAs)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    // Read and validate a token, returning its contents if valid
    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
