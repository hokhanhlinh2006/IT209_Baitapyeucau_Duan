package com.rikkeibank.identity.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTtlMs;
    private final long refreshTtlMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.access-ttl-minutes:15}") long accessMinutes,
                      @Value("${jwt.refresh-ttl-days:30}") long refreshDays) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlMs = accessMinutes * 60_000L;
        this.refreshTtlMs = refreshDays * 24 * 3_600_000L;
    }

    public String generateAccess(Long uid, String role, int tokenVersion) {
        return build(uid, role, tokenVersion, "access", accessTtlMs);
    }

    public String generateRefresh(Long uid, String role, int tokenVersion) {
        return build(uid, role, tokenVersion, "refresh", refreshTtlMs);
    }

    private String build(Long uid, String role, int tv, String type, long ttl) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(uid))
                .claim("role", role)
                .claim("tv", tv)
                .claim("type", type)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttl))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
    }
}
