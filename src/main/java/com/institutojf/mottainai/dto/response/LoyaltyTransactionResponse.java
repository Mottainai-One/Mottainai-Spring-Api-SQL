package com.institutojf.mottainai.dto.response;

import com.institutojf.mottainai.model.LoyaltyTransaction;

import java.time.LocalDateTime;

public record LoyaltyTransactionResponse(
    Integer id,
    Integer loyaltyAccountId,
    String transactionType,
    Integer points,
    String description,
    LocalDateTime createdAt
) {
    public static LoyaltyTransactionResponse fromEntity(LoyaltyTransaction transaction) {
        return new LoyaltyTransactionResponse(
            transaction.getId(),
            transaction.getLoyaltyAccount().getId(),
            transaction.getTransactionType(),
            transaction.getPoints(),
            transaction.getDescription(),
            transaction.getCreatedAt()
        );
    }
}
