package com.vitorsaucedo.janus.api.auth;

import com.vitorsaucedo.janus.api.auth.dto.AuthResponse;
import com.vitorsaucedo.janus.api.auth.dto.LoginRequest;
import com.vitorsaucedo.janus.api.auth.dto.RefreshRequest;
import com.vitorsaucedo.janus.api.auth.dto.RegisterRequest;
import com.vitorsaucedo.janus.domain.token.PasswordResetService;
import com.vitorsaucedo.janus.domain.token.RefreshTokenService;
import com.vitorsaucedo.janus.domain.user.User;
import com.vitorsaucedo.janus.domain.user.UserRepository;
import com.vitorsaucedo.janus.domain.user.UserRole;
import com.vitorsaucedo.janus.domain.user.UserService;
import com.vitorsaucedo.janus.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final PasswordResetService passwordResetService;

    @Value("${janus.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            RefreshTokenService refreshTokenService,
            UserService userService,
            PasswordResetService passwordResetService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
        this.passwordResetService = passwordResetService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        var user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.USER);

        userRepository.save(user);

        userService.savePasswordHistory(user, request.password());

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (Exception ex) {
            userService.handleLoginFailure(request.email());
            throw ex;
        }

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userService.handleLoginSuccess(user);

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(RefreshRequest request) {
        var newRefreshToken = refreshTokenService.rotate(request.refreshToken());
        var accessToken = jwtService.generateAccessToken(newRefreshToken.getUser());
        return AuthResponse.of(accessToken, newRefreshToken.getToken(), accessTokenExpiration);
    }

    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    public void forgotPassword(String email) {
        passwordResetService.requestReset(email);
    }

    public void resetPassword(String token, String newPassword) {
        passwordResetService.resetPassword(token, newPassword);
    }

    private AuthResponse buildAuthResponse(User user) {
        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = refreshTokenService.create(user);
        return AuthResponse.of(accessToken, refreshToken.getToken(), accessTokenExpiration);
    }
}
