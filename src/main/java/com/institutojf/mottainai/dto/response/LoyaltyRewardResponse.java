package com.institutojf.mottainai.dto.response;

import com.institutojf.mottainai.model.LoyaltyReward;

import java.time.LocalDateTime;

public record LoyaltyRewardResponse(
    Integer id,
    String name,
    String description,
    Integer pointsCost,
    Boolean active,
    LocalDateTime validFrom,
    LocalDateTime validUntil,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static LoyaltyRewardResponse fromEntity(LoyaltyReward reward) {
        return new LoyaltyRewardResponse(
            reward.getId(),
            reward.getName(),
            reward.getDescription(),
            reward.getPointsCost(),
            reward.getActive(),
            reward.getValidFrom(),
            reward.getValidUntil(),
            reward.getCreatedAt(),
            reward.getUpdatedAt()
        );
    }
}
