package com.institutojf.mottainai.controller.swagger;

import com.institutojf.mottainai.dto.request.CreateSuggestedActionRequest;
import com.institutojf.mottainai.dto.response.SuggestedActionResponse;
import com.institutojf.mottainai.handler.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Suggested Actions", description = "API for managing suggested actions")
public interface SuggestedActionControllerApi {

    @Operation(summary = "Find suggested actions by store")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suggested actions found", content = @Content(schema = @Schema(implementation = SuggestedActionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    List<SuggestedActionResponse> getActionsByStore(Integer storeId);

    @Operation(summary = "Find a suggested action by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suggested action found", content = @Content(schema = @Schema(implementation = SuggestedActionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Suggested action not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    SuggestedActionResponse getActionById(Integer id);

    @Operation(summary = "Create a suggested action")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Suggested action created", content = @Content(schema = @Schema(implementation = SuggestedActionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Alert not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    SuggestedActionResponse createSuggestedAction(CreateSuggestedActionRequest request);

    @Operation(summary = "Approve a suggested action")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suggested action approved", content = @Content(schema = @Schema(implementation = SuggestedActionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Suggested action not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    SuggestedActionResponse approveAction(Integer id);

    @Operation(summary = "Reject a suggested action")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suggested action rejected", content = @Content(schema = @Schema(implementation = SuggestedActionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Suggested action not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    SuggestedActionResponse rejectAction(Integer id);
}
