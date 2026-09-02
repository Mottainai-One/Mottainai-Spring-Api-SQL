package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.CreatePromotionItemRequest;
import com.institutojf.mottainai.dto.response.PromotionItemResponse;
import com.institutojf.mottainai.exception.BusinessException;
import com.institutojf.mottainai.exception.ResourceNotFoundException;
import com.institutojf.mottainai.model.Product;
import com.institutojf.mottainai.model.Promotion;
import com.institutojf.mottainai.model.PromotionItem;
import com.institutojf.mottainai.repository.ProductRepository;
import com.institutojf.mottainai.repository.PromotionItemRepository;
import com.institutojf.mottainai.repository.PromotionRepository;
import com.institutojf.mottainai.security.InventoryAccess;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PromotionItemService {

    private final PromotionItemRepository promotionItemRepository;
    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final InventoryAccess inventoryAccess;

    public PromotionItemService(PromotionItemRepository promotionItemRepository, PromotionRepository promotionRepository, ProductRepository productRepository, InventoryAccess inventoryAccess) {
        this.promotionItemRepository = promotionItemRepository;
        this.promotionRepository = promotionRepository;
        this.productRepository = productRepository;
        this.inventoryAccess = inventoryAccess;
    }

    @Transactional
    public PromotionItemResponse createPromotionItem(Integer promotionId, CreatePromotionItemRequest request, Authentication authentication) {
        if (!promotionId.equals(request.promotionId())) {
            throw new BusinessException("Promotion id must match the request path");
        }
        Promotion promotion = findAccessiblePromotion(promotionId, authentication);

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (promotionItemRepository.existsByPromotion_IdAndProduct_Id(request.promotionId(), request.productId())) {
            throw new BusinessException("Product is already in this promotion");
        }

        if (request.promotionalPrice().compareTo(request.originalPrice()) > 0) {
            throw new BusinessException("Promotional price cannot be greater than original price");
        }

        PromotionItem item = new PromotionItem();
        item.setPromotion(promotion);
        item.setProduct(product);
        item.setOriginalPrice(request.originalPrice());
        item.setPromotionalPrice(request.promotionalPrice());
        item.setQuantityAvailable(request.quantityAvailable());
        item.setCreatedAt(LocalDateTime.now());

        return PromotionItemResponse.fromEntity(promotionItemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<PromotionItemResponse> getItemsByPromotion(Integer promotionId, Authentication authentication) {
        findAccessiblePromotion(promotionId, authentication);
        return promotionItemRepository.findByPromotion_Id(promotionId).stream()
                .map(PromotionItemResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void deletePromotionItem(Integer promotionId, Integer id, Authentication authentication) {
        PromotionItem item = promotionItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion item not found"));
        if (!promotionId.equals(item.getPromotion().getId())) {
            throw new ResourceNotFoundException("Promotion item not found");
        }
        inventoryAccess.checkStoreAccess(authentication, item.getPromotion().getStore().getId());
        promotionItemRepository.delete(item);
    }

    private Promotion findAccessiblePromotion(Integer promotionId, Authentication authentication) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
        inventoryAccess.checkStoreAccess(authentication, promotion.getStore().getId());
        return promotion;
    }
}
