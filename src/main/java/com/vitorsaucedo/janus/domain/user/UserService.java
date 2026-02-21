package com.vitorsaucedo.janus.domain.user;

import com.vitorsaucedo.janus.audit.AuditService;
import com.vitorsaucedo.janus.audit.SecurityEvent.SecurityEventType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Value("${janus.security.max-login-attempts}")
    private int maxLoginAttempts;

    @Value("${janus.security.lock-duration-minutes}")
    private int lockDurationMinutes;

    @Value("${janus.security.password-history-limit}")
    private int passwordHistoryLimit;

    public UserService(
            UserRepository userRepository,
            PasswordHistoryRepository passwordHistoryRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.passwordHistoryRepository = passwordHistoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional
    public void handleLoginSuccess(User user) {
        if (user.getFailedLoginAttempts() > 0 || user.getLockedUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }

    @Transactional
    public void handleLoginFailure(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);

            if (attempts >= maxLoginAttempts) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(lockDurationMinutes));
                user.setFailedLoginAttempts(0);
                auditService.log(email, SecurityEventType.ACCOUNT_LOCKED);
            }

            userRepository.save(user);
        });
    }

    @Transactional
    public void savePasswordHistory(User user, String rawPassword) {
        var recentPasswords = passwordHistoryRepository.findLastByUser(
                user, PageRequest.of(0, passwordHistoryLimit)
        );

        boolean isReused = recentPasswords.stream()
                .anyMatch(ph -> passwordEncoder.matches(rawPassword, ph.getPasswordHash()));

        if (isReused) {
            throw new IllegalArgumentException(
                    "Password was used recently. Please choose a different password."
            );
        }

        var history = new PasswordHistory();
        history.setUser(user);
        history.setPasswordHash(passwordEncoder.encode(rawPassword));
        passwordHistoryRepository.save(history);
    }
}