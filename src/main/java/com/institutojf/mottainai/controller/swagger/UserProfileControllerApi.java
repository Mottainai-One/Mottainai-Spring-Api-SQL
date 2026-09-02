package com.institutojf.mottainai.controller.swagger;

import com.institutojf.mottainai.dto.request.InviteStoreUserRequest;
import com.institutojf.mottainai.dto.request.UpdateStoreUserRequest;
import com.institutojf.mottainai.dto.response.InviteStoreUserResponse;
import com.institutojf.mottainai.dto.response.RetailStoreResponse;
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

import java.util.List;

@Tag(name = "User Profiles", description = "API for managing user profiles")
public interface UserProfileControllerApi {

    @Operation(summary = "Get the authenticated user profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile found", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<UserResponse> me(Authentication authentication);

    @Operation(summary = "Get the authenticated user's store")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Store found", content = @Content(schema = @Schema(implementation = RetailStoreResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<RetailStoreResponse> myStore(Authentication authentication);

    @Operation(summary = "Find all store users")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Store users found", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    })
    ResponseEntity<List<UserResponse>> findAll();

    @Operation(summary = "Find a store user by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Store user found", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<UserResponse> findById(Integer id);

    @Operation(summary = "Invite a user to the store")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Store user invited", content = @Content(schema = @Schema(implementation = InviteStoreUserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Requester or role not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Email or CPF already exists", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<InviteStoreUserResponse> invite(InviteStoreUserRequest request, Authentication authentication);

    @Operation(summary = "Update a store user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Store user updated", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "User or role not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<UserResponse> update(Integer id, UpdateStoreUserRequest request);
}
