package com.institutojf.mottainai.controller;

import com.institutojf.mottainai.controller.swagger.LoyaltyControllerApi;
import com.institutojf.mottainai.dto.request.RedeemRewardRequest;
import com.institutojf.mottainai.dto.response.LoyaltyAccountResponse;
import com.institutojf.mottainai.dto.response.LoyaltyTransactionResponse;
import com.institutojf.mottainai.security.CustomerAccess;
import com.institutojf.mottainai.service.LoyaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/client/loyalty")
@RequiredArgsConstructor
public class LoyaltyController implements LoyaltyControllerApi {

    private final LoyaltyService loyaltyService;

    private final CustomerAccess customerAccess;

    @Override
    @GetMapping("/balance")
    public LoyaltyAccountResponse getBalance(Authentication authentication) {
        return loyaltyService.getLoyaltyAccount(customerAccess.currentCustomer(authentication).getId());
    }

    @Override
    @GetMapping("/transactions")
    public List<LoyaltyTransactionResponse> getTransactions(Authentication authentication, @RequestParam LocalDateTime from, @RequestParam LocalDateTime to) {
        return loyaltyService.getTransactions(customerAccess.currentCustomer(authentication).getId(), from, to);
    }

    @Override
    @PostMapping("/redeem")
    @ResponseStatus(HttpStatus.OK)
    public void redeemReward(@Valid @RequestBody RedeemRewardRequest request, Authentication authentication, @RequestHeader("Idempotency-Key") String idempotencyKey) {
        loyaltyService.redeemReward(customerAccess.currentCustomer(authentication).getId(), request, idempotencyKey);
    }

}
