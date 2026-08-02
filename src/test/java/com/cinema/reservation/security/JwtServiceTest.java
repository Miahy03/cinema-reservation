package com.cinema.reservation.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cinema.reservation.model.UserRole;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService =
        new JwtService(
            "unit-test-secret-unit-test-secret-unit-test-secret-12345", "test-issuer", 3600);
  }

  @Test
  void generate_and_parse_roundtrip() {
    String token = jwtService.generateToken("user-1", UserRole.CLIENT);

    JwtClaims claims = jwtService.parse(token);

    assertThat(claims.userId()).isEqualTo("user-1");
    assertThat(claims.role()).isEqualTo(UserRole.CLIENT);
  }

  @Test
  void parse_invalid_signature_throws() {
    JwtService other =
        new JwtService("other-secret-other-secret-other-secret-other-secret", "test-issuer", 3600);
    String token = other.generateToken("user-1", UserRole.MANAGER);

    assertThatThrownBy(() -> jwtService.parse(token)).isInstanceOf(JwtException.class);
  }

  @Test
  void parse_expired_token_throws() {
    JwtService expired =
        new JwtService(
            "unit-test-secret-unit-test-secret-unit-test-secret-12345", "test-issuer", -1);
    String token = expired.generateToken("user-1", UserRole.CLIENT);

    assertThatThrownBy(() -> jwtService.parse(token)).isInstanceOf(JwtException.class);
  }
}
