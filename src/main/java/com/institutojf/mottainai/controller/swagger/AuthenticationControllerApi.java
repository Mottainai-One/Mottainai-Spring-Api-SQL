package com.institutojf.mottainai.controller.swagger;

import com.institutojf.mottainai.dto.request.ForgotPasswordRequest;
import com.institutojf.mottainai.dto.request.LoginRequest;
import com.institutojf.mottainai.dto.request.ResetPasswordRequest;
import com.institutojf.mottainai.dto.response.TokenResponse;
import com.institutojf.mottainai.handler.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Authentication", description = "API for authentication and password recovery")
public interface AuthenticationControllerApi {

    @Operation(summary = "Authenticate a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User authenticated", content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<TokenResponse> login(LoginRequest request);

    @Operation(summary = "Request a password reset code")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password reset code requested"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<Void> forgotPassword(ForgotPasswordRequest request);

    @Operation(summary = "Reset a password")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password reset"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Password reset token not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<Void> resetPassword(ResetPasswordRequest request);
}
