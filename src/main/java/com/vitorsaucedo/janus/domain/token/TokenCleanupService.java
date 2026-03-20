package com.vitorsaucedo.janus.domain.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TokenCleanupService {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${janus.security.token-cleanup.revoked-retention-days:7}")
    private int revokedRetentionDays;

    public TokenCleanupService(
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Scheduled(cron = "${janus.security.token-cleanup.cron:0 0 0 * * *}")
    @Transactional
    public void cleanUpExpiredTokens() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(revokedRetentionDays);

        refreshTokenRepository.deleteExpiredAndRevoked(threshold);
        passwordResetTokenRepository.deleteExpiredAndUsed(threshold);

        log.info("Token cleanup completed. Threshold: {}", threshold);
    }
}