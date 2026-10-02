package com.institutojf.mottainai.service;

import com.institutojf.mottainai.exception.BusinessException;
import com.institutojf.mottainai.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class LoyaltyServiceTest {

    @Mock
    private LoyaltyAccountRepository accountRepository;

    @Mock
    private LoyaltyTransactionRepository transactionRepository;

    @Mock
    private LoyaltyRewardRepository rewardRepository;

    @Mock
    private LoyaltyRedemptionRepository redemptionRepository;

    @Mock
    private OutboxEventRepository outboxRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private LoyaltyService service;

    @Test
    @DisplayName("Should reject transaction range longer than six months")
    void rejectsTransactionRangeLongerThanSixMonths() {
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        assertThrows(BusinessException.class, () -> service.getTransactions(1, from, from.plusMonths(6).plusDays(1)));
        verifyNoInteractions(accountRepository, transactionRepository);
    }

}
