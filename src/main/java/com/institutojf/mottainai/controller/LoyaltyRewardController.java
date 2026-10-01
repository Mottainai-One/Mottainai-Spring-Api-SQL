package com.institutojf.mottainai.controller;

import com.institutojf.mottainai.controller.swagger.LoyaltyRewardControllerApi;
import com.institutojf.mottainai.dto.request.CreateLoyaltyRewardRequest;
import com.institutojf.mottainai.dto.request.UpdateLoyaltyRewardRequest;
import com.institutojf.mottainai.dto.response.LoyaltyRewardResponse;
import com.institutojf.mottainai.service.LoyaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loyalty/rewards")
@RequiredArgsConstructor
public class LoyaltyRewardController implements LoyaltyRewardControllerApi {

    private final LoyaltyService loyaltyService;

    @Override
    @GetMapping
    public List<LoyaltyRewardResponse> findAll() {
        return loyaltyService.getActiveRewards();
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','MANAGER')")
    public LoyaltyRewardResponse create(@Valid @RequestBody CreateLoyaltyRewardRequest request) {
        return loyaltyService.createReward(request);
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','MANAGER')")
    public LoyaltyRewardResponse update(@PathVariable Integer id, @Valid @RequestBody UpdateLoyaltyRewardRequest request) {
        return loyaltyService.updateReward(id, request);
    }

}
