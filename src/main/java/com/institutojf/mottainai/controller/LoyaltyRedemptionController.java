package com.institutojf.mottainai.controller;

import com.institutojf.mottainai.controller.swagger.LoyaltyRedemptionControllerApi;
import com.institutojf.mottainai.dto.request.RedeemRewardRequest;
import com.institutojf.mottainai.security.CustomerAccess;
import com.institutojf.mottainai.service.LoyaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/loyalty/redemptions")
@RequiredArgsConstructor
public class LoyaltyRedemptionController implements LoyaltyRedemptionControllerApi {

    private final LoyaltyService loyaltyService;

    private final CustomerAccess customerAccess;

    @Override
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public void redeem(@Valid @RequestBody RedeemRewardRequest request, Authentication authentication, @RequestHeader("Idempotency-Key") String idempotencyKey) {
        loyaltyService.redeemReward(customerAccess.currentCustomer(authentication).getId(), request, idempotencyKey);
    }

}
