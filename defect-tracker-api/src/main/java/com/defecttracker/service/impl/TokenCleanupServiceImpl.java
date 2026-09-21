package com.defecttracker.service.impl;

import com.defecttracker.repository.PasswordResetTokenRepository;
import com.defecttracker.repository.RefreshTokenRepository;
import com.defecttracker.service.TokenCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TokenCleanupServiceImpl implements TokenCleanupService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    @Transactional
    @Scheduled(cron = "0 */10 * * * *")
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        int deletedRefresh = refreshTokenRepository.deleteExpiredTokens(now);
        int deletedReset = passwordResetTokenRepository.deleteExpiredOrUsedTokens(now);
        log.info("Token cleanup completed: purged {} expired refresh tokens and {} expired/used reset tokens",
                deletedRefresh, deletedReset);
    }
}
