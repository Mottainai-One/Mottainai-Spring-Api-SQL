package com.institutojf.mottainai.controller.swagger;

import com.institutojf.mottainai.handler.ApiError;
import com.institutojf.mottainai.dto.response.CustomerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;

@Tag(name = "CustomerProfile", description = "Authenticated customer profile")
public interface CustomerProfileControllerApi {

    @Operation(summary = "Get the current customer profile using an internal access token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer profile found", content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Customer role required"),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    CustomerResponse profile(Authentication authentication);

    @Operation(summary = "Get the current customer profile using a Firebase ID token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer profile found", content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
            @ApiResponse(responseCode = "401", description = "Firebase authentication required"),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    CustomerResponse clientProfile(Authentication authentication);

}
