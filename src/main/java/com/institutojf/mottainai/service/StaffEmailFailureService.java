package com.institutojf.mottainai.service;

import com.institutojf.mottainai.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class StaffEmailFailureService {
    private final RlsContextService rlsContextService;
    private final PasswordResetTokenRepository tokenRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void invalidateRecoveryToken(String hash) {
        Integer userId = rlsContextService.bootstrapByToken(hash, "RECOVERY");
        if (userId != null) {
            tokenRepository.findUnusedByHashForUpdate(hash).ifPresent(token -> {
                token.setUsedAt(OffsetDateTime.now(ZoneOffset.UTC));
                tokenRepository.save(token);
            });
        }
    }
}
