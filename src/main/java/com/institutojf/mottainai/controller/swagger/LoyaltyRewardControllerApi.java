package com.institutojf.mottainai.controller.swagger;

import com.institutojf.mottainai.dto.request.CreateLoyaltyRewardRequest;
import com.institutojf.mottainai.dto.request.UpdateLoyaltyRewardRequest;
import com.institutojf.mottainai.dto.response.LoyaltyRewardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "LoyaltyReward", description = "API for LoyaltyReward")
public interface LoyaltyRewardControllerApi {

    @Operation(summary = "findAll")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response")
    })
    List<LoyaltyRewardResponse> findAll();

    @Operation(summary = "create")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Successful response")
    })
    LoyaltyRewardResponse create(CreateLoyaltyRewardRequest request);

    @Operation(summary = "update")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response")
    })
    LoyaltyRewardResponse update(Integer id, UpdateLoyaltyRewardRequest request);

}
