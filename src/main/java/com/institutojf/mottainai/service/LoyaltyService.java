package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.CreateLoyaltyRewardRequest;
import com.institutojf.mottainai.dto.request.RedeemRewardRequest;
import com.institutojf.mottainai.dto.request.UpdateLoyaltyRewardRequest;
import com.institutojf.mottainai.dto.response.LoyaltyAccountResponse;
import com.institutojf.mottainai.dto.response.LoyaltyRewardResponse;
import com.institutojf.mottainai.dto.response.LoyaltyTransactionResponse;
import com.institutojf.mottainai.exception.BusinessException;
import com.institutojf.mottainai.exception.ConflictException;
import com.institutojf.mottainai.exception.ResourceNotFoundException;
import com.institutojf.mottainai.model.LoyaltyAccount;
import com.institutojf.mottainai.model.LoyaltyRedemption;
import com.institutojf.mottainai.model.LoyaltyReward;
import com.institutojf.mottainai.model.LoyaltyTransaction;
import com.institutojf.mottainai.repository.LoyaltyAccountRepository;
import com.institutojf.mottainai.repository.LoyaltyRedemptionRepository;
import com.institutojf.mottainai.repository.LoyaltyRewardRepository;
import com.institutojf.mottainai.repository.LoyaltyTransactionRepository;
import com.institutojf.mottainai.repository.OutboxEventRepository;
import com.institutojf.mottainai.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final LoyaltyAccountRepository loyaltyAccountRepository;

    private final LoyaltyTransactionRepository loyaltyTransactionRepository;

    private final LoyaltyRewardRepository loyaltyRewardRepository;

    private final LoyaltyRedemptionRepository loyaltyRedemptionRepository;

    private final OutboxEventRepository outboxEventRepository;

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public LoyaltyAccountResponse getLoyaltyAccount(Integer customerId) {
        LoyaltyAccount account = loyaltyAccountRepository.findByCustomer_Id(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Loyalty account not found"));
        return LoyaltyAccountResponse.fromEntity(account);
    }

    @Transactional(readOnly = true)
    public List<LoyaltyTransactionResponse> getTransactions(Integer customerId, LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null || to.isBefore(from) || to.isAfter(from.plusMonths(6))) {
            throw new BusinessException("A valid transaction date range of at most six months is required");
        }
        LoyaltyAccount account = loyaltyAccountRepository.findByCustomer_Id(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Loyalty account not found"));
        return loyaltyTransactionRepository
            .findByLoyaltyAccount_IdAndCreatedAtBetweenOrderByCreatedAtDesc(account.getId(), from, to)
            .stream()
            .map(LoyaltyTransactionResponse::fromEntity)
            .toList();
    }

    @Transactional
    public LoyaltyRewardResponse createReward(CreateLoyaltyRewardRequest request) {
        LoyaltyReward reward = new LoyaltyReward();
        reward.setName(request.name());
        reward.setDescription(request.description());
        reward.setPointsCost(request.pointsCost());
        reward.setActive(request.active() != null ? request.active() : true);
        reward.setValidFrom(request.validFrom());
        reward.setValidUntil(request.validUntil());
        reward.setCreatedAt(LocalDateTime.now());
        reward.setUpdatedAt(LocalDateTime.now());
        loyaltyRewardRepository.save(reward);
        auditLogRepository.record("loyalty_reward", "INSERT", reward.getId().toString(), null, null,
                Map.of("name", reward.getName(), "points_cost", reward.getPointsCost()));
        return LoyaltyRewardResponse.fromEntity(reward);
    }

    @Transactional
    public LoyaltyRewardResponse updateReward(Integer id, UpdateLoyaltyRewardRequest request) {
        LoyaltyReward reward = loyaltyRewardRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reward not found"));
        Integer oldCost = reward.getPointsCost();
        reward.setPointsCost(request.pointsCost());
        reward.setUpdatedAt(LocalDateTime.now());
        loyaltyRewardRepository.save(reward);
        auditLogRepository.record("loyalty_reward", "UPDATE", id.toString(), null, Map.of("points_cost", oldCost),
                Map.of("points_cost", reward.getPointsCost()));
        return LoyaltyRewardResponse.fromEntity(reward);
    }

    @Transactional(readOnly = true)
    public List<LoyaltyRewardResponse> getActiveRewards() {
        return loyaltyRewardRepository.findByActiveTrueOrderByName()
            .stream()
            .map(LoyaltyRewardResponse::fromEntity)
            .toList();
    }

    @Transactional
    public void redeemReward(Integer customerId, RedeemRewardRequest request, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 180) {
            throw new BusinessException("Idempotency-Key must contain between 1 and 180 characters");
        }
        outboxEventRepository.lockIdempotencyKey(idempotencyKey);
        if (isIdempotentReplay(customerId, request.rewardId(), idempotencyKey)) {
            return;
        }
        LoyaltyAccount account = loyaltyAccountRepository.findByCustomerIdForUpdate(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Loyalty account not found"));
        LoyaltyReward reward = loyaltyRewardRepository.findById(request.rewardId())
            .orElseThrow(() -> new ResourceNotFoundException("Reward not found"));
        LocalDateTime now = LocalDateTime.now();
        if (!Boolean.TRUE.equals(account.getActive())) {
            throw new BusinessException("Loyalty account is not active");
        }
        if (!reward.getActive() || reward.getValidFrom() != null && reward.getValidFrom().isAfter(now)
                || reward.getValidUntil() != null && !reward.getValidUntil().isAfter(now)) {
            throw new BusinessException("Reward is not active");
        }
        if (account.getPointsBalance() < reward.getPointsCost()) {
            throw new BusinessException("Insufficient points balance");
        }
        account.setPointsBalance(account.getPointsBalance() - reward.getPointsCost());
        account.setUpdatedAt(LocalDateTime.now());
        LoyaltyRedemption redemption = new LoyaltyRedemption();
        redemption.setLoyaltyAccount(account);
        redemption.setReward(reward);
        redemption.setPointsSpent(reward.getPointsCost());
        redemption.setRedeemedAt(LocalDateTime.now());
        redemption.setStatus("CONFIRMED");
        LoyaltyTransaction transaction = new LoyaltyTransaction();
        transaction.setLoyaltyAccount(account);
        transaction.setTransactionType("REDEEM");
        transaction.setPoints(-reward.getPointsCost());
        transaction.setDescription("Redeemed: " + reward.getName());
        transaction.setCreatedAt(LocalDateTime.now());
        loyaltyAccountRepository.save(account);
        loyaltyRedemptionRepository.saveAndFlush(redemption);
        loyaltyTransactionRepository.save(transaction);
        outboxEventRepository.publishLoyaltyRedemption(redemption.getId().toString(), customerId, request.rewardId(),
                reward.getPointsCost(), idempotencyKey);
    }

    private boolean isIdempotentReplay(Integer customerId, Integer rewardId, String idempotencyKey) {
        var previous = outboxEventRepository.findByIdempotencyKey(idempotencyKey);
        if (previous.isEmpty()) {
            return false;
        }
        if (!outboxEventRepository.isLoyaltyRedemptionFor(previous.get(), customerId, rewardId)) {
            throw new ConflictException("Idempotency-Key was already used for a different request");
        }
        return true;
    }

}
