package com.br3akPoint.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

public class JWTUtil {

    private final SecretKey signingKey;
    private final long      accessTokenExpiryMs;
    private final long      refreshTokenExpiryMs;

    // ── Constructor (no Spring annotations — library stays framework-agnostic) ─

    public JWTUtil(String secretKey, long accessTokenExpiryMs, long refreshTokenExpiryMs) {
        this.signingKey           = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiryMs  = accessTokenExpiryMs;
        this.refreshTokenExpiryMs = refreshTokenExpiryMs;
    }

    // ── Access Token ──────────────────────────────────────────────────────────

    /**
     * Generate a signed JWT access token for the given user.
     * Expires in 5 minutes.
     */
    public String generateAccessToken(Object userId, String email, List<String> roles, Map<String, Object> additionalClaims) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId",   userId);
        claims.put("email",    email);
        claims.put("roles",    roles);

        claims.putAll(additionalClaims);

        return Jwts.builder()
                .subject(email)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiryMs))
                .signWith(signingKey)
                .compact();
    }

    public String generateAccessToken(Object userId, String email, Map<String, Object> claims) {
        return generateAccessToken(userId, email, List.of(), claims);
    }

    public String generateAccessToken(Object userId, String email) {
        return generateAccessToken(userId, email, List.of(), Map.of());
    }

    // ── Refresh Token ─────────────────────────────────────────────────────────

    /**
     * Generate a random UUID refresh token string.
     * Callers must persist this + its expiry date in the database.
     */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Expiry date to persist alongside the refresh token.
     */
    public Date getRefreshTokenExpiry() {
        return new Date(System.currentTimeMillis() + refreshTokenExpiryMs);
    }

    // ── Validation ────────────────────────────────────────────────────────────

    public boolean isAccessTokenValid(String token, String expectedUsername) {
        try {
            String username = extractUsername(token);
            return username.equals(expectedUsername) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ── Claim Extractors ──────────────────────────────────────────────────────

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAll(String token) {
        return extractAllClaims(token);
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
