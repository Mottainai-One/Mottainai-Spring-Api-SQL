package com.institutojf.mottainai.controller;

import com.institutojf.mottainai.controller.swagger.PromotionControllerApi;

import com.institutojf.mottainai.dto.request.CreatePromotionRequest;
import com.institutojf.mottainai.dto.request.UpdatePromotionRequest;
import com.institutojf.mottainai.dto.response.PromotionResponse;
import com.institutojf.mottainai.service.PromotionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/promotions")
public class PromotionController implements PromotionControllerApi {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @Override
    @GetMapping
    public List<PromotionResponse> getPromotionsByStore(@RequestParam Integer storeId, Authentication authentication) {
        return promotionService.getPromotionsByStore(storeId, authentication);
    }

    @Override
    @GetMapping("/{id}")
    public PromotionResponse getPromotionById(@PathVariable Integer id, Authentication authentication) {
        return promotionService.getPromotionById(id, authentication);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PromotionResponse createPromotion(@Valid @RequestBody CreatePromotionRequest request, Authentication authentication) {
        return promotionService.createPromotion(request, authentication);
    }

    @Override
    @PutMapping("/{id}")
    public PromotionResponse updatePromotion(@PathVariable Integer id, @Valid @RequestBody UpdatePromotionRequest request, Authentication authentication) {
        return promotionService.updatePromotion(id, request, authentication);
    }

    @Override
    @PostMapping("/{id}/activate")
    public PromotionResponse activatePromotion(@PathVariable Integer id, Authentication authentication) {
        return promotionService.activatePromotion(id, authentication);
    }

    @Override
    @PostMapping("/{id}/deactivate")
    public PromotionResponse deactivatePromotion(@PathVariable Integer id, Authentication authentication) {
        return promotionService.deactivatePromotion(id, authentication);
    }
}
