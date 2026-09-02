package com.institutojf.mottainai.dto.response;

import com.institutojf.mottainai.model.LoyaltyAccount;

import java.time.LocalDateTime;

public record LoyaltyAccountResponse(
    Integer id,
    Integer customerId,
    String customerName,
    String customerEmail,
    Integer pointsBalance,
    LocalDateTime joinedAt,
    Boolean active,
    LocalDateTime updatedAt
) {
    public static LoyaltyAccountResponse fromEntity(LoyaltyAccount account) {
        return new LoyaltyAccountResponse(
            account.getId(),
            account.getCustomer().getId(),
            account.getCustomer().getFullName(),
            account.getCustomer().getEmail(),
            account.getPointsBalance(),
            account.getJoinedAt(),
            account.getActive(),
            account.getUpdatedAt()
        );
    }
}
