package com.vitorsaucedo.janus.api;

import com.vitorsaucedo.janus.api.auth.AuthService;
import com.vitorsaucedo.janus.api.auth.dto.LoginRequest;
import com.vitorsaucedo.janus.api.auth.dto.RegisterRequest;
import com.vitorsaucedo.janus.domain.token.PasswordResetService;
import com.vitorsaucedo.janus.domain.token.RefreshToken;
import com.vitorsaucedo.janus.domain.token.RefreshTokenService;
import com.vitorsaucedo.janus.domain.user.User;
import com.vitorsaucedo.janus.domain.user.UserRepository;
import com.vitorsaucedo.janus.domain.user.UserRole;
import com.vitorsaucedo.janus.domain.user.UserService;
import com.vitorsaucedo.janus.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private UserService userService;
    @Mock private PasswordResetService passwordResetService;

    @InjectMocks
    private AuthService authService;

    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "accessTokenExpiration", 900000L);

        user = new User();
        user.setEmail("vitor@janus.dev");
        user.setName("Vitor");
        user.setRole(UserRole.USER);

        refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-uuid");
        refreshToken.setUser(user);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        var request = new RegisterRequest("Vitor", "vitor@janus.dev", "Test@1234");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(refreshTokenService.create(any())).thenReturn(refreshToken);

        var response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token-uuid");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowWhenEmailAlreadyInUse() {
        var request = new RegisterRequest("Vitor", "vitor@janus.dev", "Test@1234");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already in use");

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldLoginSuccessfully() {
        var request = new LoginRequest("vitor@janus.dev", "Test@1234");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(refreshTokenService.create(user)).thenReturn(refreshToken);

        var response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        verify(userService).handleLoginSuccess(user);
    }

    @Test
    void shouldHandleLoginFailureOnBadCredentials() {
        var request = new LoginRequest("vitor@janus.dev", "wrong-password");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(userService).handleLoginFailure(request.email());
    }
}
