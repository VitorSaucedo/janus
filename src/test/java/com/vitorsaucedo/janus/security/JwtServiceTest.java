package com.vitorsaucedo.janus.security;

import com.vitorsaucedo.janus.domain.user.User;
import com.vitorsaucedo.janus.domain.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "test-secret-key-with-at-least-256-bits-for-hmac");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 900000L);

        user = new User();
        user.setEmail("vitor@janus.dev");
        user.setName("Vitor");
        user.setRole(UserRole.USER);
    }

    @Test
    void shouldGenerateValidAccessToken() {
        var token = jwtService.generateAccessToken(user);

        assertThat(token).isNotBlank();
    }

    @Test
    void shouldExtractEmailFromToken() {
        var token = jwtService.generateAccessToken(user);

        var email = jwtService.extractEmail(token);

        assertThat(email).isEqualTo("vitor@janus.dev");
    }

    @Test
    void shouldValidateTokenForCorrectUser() {
        var token = jwtService.generateAccessToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void shouldRejectTokenForDifferentUser() {
        var token = jwtService.generateAccessToken(user);

        var otherUser = new User();
        otherUser.setEmail("other@janus.dev");
        otherUser.setName("Other");
        otherUser.setRole(UserRole.USER);

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void shouldRejectExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", -1000L);

        var token = jwtService.generateAccessToken(user);

        assertThatThrownBy(() -> jwtService.isTokenValid(token, user));
    }
}
