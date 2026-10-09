package com.institutojf.mottainai.controller.swagger;

import com.institutojf.mottainai.dto.request.CustomerLoginRequest;
import com.institutojf.mottainai.dto.request.CustomerPasswordRecoveryRequest;
import com.institutojf.mottainai.dto.request.CustomerPasswordResetRequest;
import com.institutojf.mottainai.dto.request.CustomerResetTokenRequest;
import com.institutojf.mottainai.dto.response.CustomerTokenResponse;
import com.institutojf.mottainai.dto.response.TokenValidationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "CustomerAuthentication", description = "API for CustomerAuthentication")
public interface CustomerAuthenticationControllerApi {

    @Operation(summary = "login")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response")
    })
    CustomerTokenResponse login(CustomerLoginRequest request);

    @Operation(summary = "passwordRecovery")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Successful response")
    })
    void passwordRecovery(CustomerPasswordRecoveryRequest request);

    @Operation(summary = "validate")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response")
    })
    TokenValidationResponse validate(CustomerResetTokenRequest request);

    @Operation(summary = "reset")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Successful response")
    })
    void reset(CustomerPasswordResetRequest request);

}
