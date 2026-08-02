package com.cinema.reservation.security;

import com.cinema.reservation.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {
  private final SecretKey key;
  private final String issuer;
  private final long expirationSeconds;

  public JwtService(
      @Value("${app.security.jwt.secret}") String secret,
      @Value("${app.security.jwt.issuer}") String issuer,
      @Value("${app.security.jwt.expiration}") long expirationSeconds) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.issuer = issuer;
    this.expirationSeconds = expirationSeconds;
  }

  public String generateToken(String userId, UserRole role) {
    Instant now = Instant.now();
    return Jwts.builder()
        .issuer(issuer)
        .subject(userId)
        .claim("role", role.name())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(expirationSeconds)))
        .signWith(key)
        .compact();
  }

  public JwtClaims parse(String token) {
    Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    return new JwtClaims(claims.getSubject(), UserRole.valueOf(claims.get("role", String.class)));
  }
}
