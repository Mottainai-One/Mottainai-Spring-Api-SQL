package com.institutojf.mottainai.controller.swagger;

import com.institutojf.mottainai.dto.request.ChangePasswordRequest;
import com.institutojf.mottainai.dto.request.ForgotPasswordRequest;
import com.institutojf.mottainai.dto.request.LoginRequest;
import com.institutojf.mottainai.dto.request.RefreshTokenRequest;
import com.institutojf.mottainai.dto.request.ResetPasswordRequest;
import com.institutojf.mottainai.dto.response.TokenResponse;
import com.institutojf.mottainai.dto.response.TokenValidationResponse;
import com.institutojf.mottainai.dto.response.UserResponse;
import com.institutojf.mottainai.handler.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

@Tag(name = "Authentication", description = "API for authentication and password recovery")
public interface AuthenticationControllerApi {

    @Operation(summary = "Get the authenticated employee profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee profile found", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<UserResponse> profile(Authentication authentication);

    @Operation(summary = "Authenticate a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User authenticated", content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<TokenResponse> login(LoginRequest request);

    @Operation(summary = "Request a password reset link using CPF and email")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Request accepted"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<Void> passwordRecovery(ForgotPasswordRequest request);

    @Operation(summary = "Validate a password reset or invitation token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token validity returned; invalid or expired tokens have valid=false", content = @Content(schema = @Schema(implementation = TokenValidationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Missing token parameter", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<TokenValidationResponse> validateResetToken(String token);

    @Operation(summary = "Reset a password")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password reset"),
            @ApiResponse(responseCode = "400", description = "Invalid request or expired token", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<Void> passwordReset(ResetPasswordRequest request);

    @Operation(summary = "Rotate a refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens rotated", content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or unusable refresh token", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    ResponseEntity<TokenResponse> refresh(RefreshTokenRequest request);

    @Operation(summary = "Revoke the current staff session")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Session revoked"),
            @ApiResponse(responseCode = "400", description = "Invalid request or refresh token", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    ResponseEntity<Void> logout(RefreshTokenRequest request, Authentication authentication);

    @Operation(summary = "Change the authenticated employee password")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed"),
            @ApiResponse(responseCode = "400", description = "Invalid request or current password", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    ResponseEntity<Void> changePassword(ChangePasswordRequest request, Authentication authentication);

}
