package com.institutojf.mottainai.controller.swagger;

import com.institutojf.mottainai.dto.request.RedeemRewardRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;

@Tag(name = "LoyaltyRedemption", description = "API for LoyaltyRedemption")
public interface LoyaltyRedemptionControllerApi {

    @Operation(summary = "redeem")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response")
    })
    void redeem(RedeemRewardRequest request, Authentication authentication, String idempotencyKey);

}
