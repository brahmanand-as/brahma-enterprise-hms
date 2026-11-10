package com.brahma.arogya.security.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey key;

  public JwtService(@Value("${app.jwt-secret}") String secret) {
    if (secret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32)
      throw new IllegalStateException("HMS_JWT_SECRET must be at least 32 bytes");
    key = Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }

  public String issue(String username) {
    return issue(username, 0);
  }

  public String issue(String username, int version) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(username)
        .claim("ver", version)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(3600)))
        .signWith(key)
        .compact();
  }

  public int version(String token) {
    Object v =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("ver");
    return v instanceof Number n ? n.intValue() : 0;
  }

  public String subject(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
  }
}
