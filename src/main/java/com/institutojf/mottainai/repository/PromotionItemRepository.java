package com.institutojf.mottainai.repository;

import com.institutojf.mottainai.model.PromotionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionItemRepository extends JpaRepository<PromotionItem, Integer> {

    List<PromotionItem> findByPromotion_Id(Integer promotionId);

    boolean existsByPromotion_IdAndProduct_Id(Integer promotionId, Integer productId);
}
