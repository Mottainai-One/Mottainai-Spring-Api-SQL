package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.CreatePromotionRequest;
import com.institutojf.mottainai.dto.request.UpdatePromotionRequest;
import com.institutojf.mottainai.dto.response.PromotionResponse;
import com.institutojf.mottainai.exception.ResourceNotFoundException;
import com.institutojf.mottainai.model.Promotion;
import com.institutojf.mottainai.model.enums.PromotionStatus;
import com.institutojf.mottainai.model.RetailStore;
import com.institutojf.mottainai.repository.PromotionRepository;
import com.institutojf.mottainai.repository.RetailStoreRepository;
import com.institutojf.mottainai.security.InventoryAccess;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final RetailStoreRepository retailStoreRepository;
    private final InventoryAccess inventoryAccess;

    public PromotionService(PromotionRepository promotionRepository, RetailStoreRepository retailStoreRepository,
                            InventoryAccess inventoryAccess) {
        this.promotionRepository = promotionRepository;
        this.retailStoreRepository = retailStoreRepository;
        this.inventoryAccess = inventoryAccess;
    }

    @Transactional
    public PromotionResponse createPromotion(CreatePromotionRequest request, Authentication authentication) {
        inventoryAccess.checkStoreAccess(authentication, request.storeId());
        RetailStore store = retailStoreRepository.findById(request.storeId())
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        Promotion promotion = new Promotion();
        promotion.setStore(store);
        promotion.setName(request.name());
        promotion.setDescription(request.description());
        promotion.setPromotionType(request.promotionType());
        promotion.setStartsAt(request.startsAt());
        promotion.setEndsAt(request.endsAt());
        promotion.setStatus(PromotionStatus.DRAFT);
        promotion.setActive(request.active() != null ? request.active() : false);
        promotion.setCreatedAt(LocalDateTime.now());
        promotion.setUpdatedAt(LocalDateTime.now());

        return PromotionResponse.fromEntity(promotionRepository.save(promotion));
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> getPromotionsByStore(Integer storeId, Authentication authentication) {
        inventoryAccess.checkStoreAccess(authentication, storeId);
        return promotionRepository.findByStore_StoreIdAndDeletedAtIsNullOrderByStartsAtDesc(storeId).stream()
                .map(PromotionResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PromotionResponse getPromotionById(Integer id, Authentication authentication) {
        Promotion promotion = findAccessiblePromotion(id, authentication);
        return PromotionResponse.fromEntity(promotion);
    }

    @Transactional
    public PromotionResponse updatePromotion(Integer id, UpdatePromotionRequest request, Authentication authentication) {
        Promotion promotion = findAccessiblePromotion(id, authentication);

        if (request.name() != null) promotion.setName(request.name());
        if (request.description() != null) promotion.setDescription(request.description());
        if (request.promotionType() != null) promotion.setPromotionType(request.promotionType());
        if (request.startsAt() != null) promotion.setStartsAt(request.startsAt());
        if (request.endsAt() != null) promotion.setEndsAt(request.endsAt());
        if (request.active() != null) promotion.setActive(request.active());
        promotion.setUpdatedAt(LocalDateTime.now());

        return PromotionResponse.fromEntity(promotionRepository.save(promotion));
    }

    @Transactional
    public PromotionResponse activatePromotion(Integer id, Authentication authentication) {
        Promotion promotion = findAccessiblePromotion(id, authentication);

        promotion.setActive(true);
        promotion.setStatus(PromotionStatus.APPROVED);
        promotion.setApprovedAt(LocalDateTime.now());
        promotion.setUpdatedAt(LocalDateTime.now());

        return PromotionResponse.fromEntity(promotionRepository.save(promotion));
    }

    @Transactional
    public PromotionResponse deactivatePromotion(Integer id, Authentication authentication) {
        Promotion promotion = findAccessiblePromotion(id, authentication);

        promotion.setActive(false);
        promotion.setUpdatedAt(LocalDateTime.now());

        return PromotionResponse.fromEntity(promotionRepository.save(promotion));
    }

    private Promotion findAccessiblePromotion(Integer id, Authentication authentication) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
        inventoryAccess.checkStoreAccess(authentication, promotion.getStore().getId());
        return promotion;
    }
}
