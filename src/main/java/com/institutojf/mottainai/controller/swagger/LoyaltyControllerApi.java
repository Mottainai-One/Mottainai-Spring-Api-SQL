package com.institutojf.mottainai.controller.swagger;

import com.institutojf.mottainai.dto.request.RedeemRewardRequest;
import com.institutojf.mottainai.dto.response.LoyaltyAccountResponse;
import com.institutojf.mottainai.dto.response.LoyaltyTransactionResponse;
import com.institutojf.mottainai.handler.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;

import java.util.List;

@Tag(name = "Loyalty", description = "Customer loyalty API")
public interface LoyaltyControllerApi {

    @Operation(summary = "Get the authenticated customer's points balance")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loyalty balance found", content = @Content(schema = @Schema(implementation = LoyaltyAccountResponse.class))),
            @ApiResponse(responseCode = "404", description = "Customer or loyalty account not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    LoyaltyAccountResponse getBalance(Authentication authentication);

    @Operation(summary = "Get the authenticated customer's loyalty transactions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loyalty transactions found", content = @Content(schema = @Schema(implementation = LoyaltyTransactionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Customer or loyalty account not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    List<LoyaltyTransactionResponse> getTransactions(Authentication authentication);

    @Operation(summary = "Redeem a reward for the authenticated customer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loyalty reward redeemed"),
            @ApiResponse(responseCode = "400", description = "Reward is inactive or balance is insufficient", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Customer, loyalty account, or reward not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    void redeemReward(RedeemRewardRequest request, Authentication authentication);
}
