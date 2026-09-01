package com.institutojf.mottainai.controller.swagger;

import com.institutojf.mottainai.dto.request.CreateBatchRequest;
import com.institutojf.mottainai.dto.response.BatchResponse;
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

@Tag(name = "Batches", description = "API for managing batches")
public interface BatchControllerApi {

    @Operation(summary = "Create a batch")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Batch created", content = @Content(schema = @Schema(implementation = BatchResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Batch code already exists", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<BatchResponse> create(CreateBatchRequest request, Authentication authentication);

    @Operation(summary = "Find all batches")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Batches found", content = @Content(schema = @Schema(implementation = BatchResponse.class)))
    })
    ResponseEntity<List<BatchResponse>> findAll(Authentication authentication);

    @Operation(summary = "Find a batch by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Batch found", content = @Content(schema = @Schema(implementation = BatchResponse.class))),
            @ApiResponse(responseCode = "404", description = "Batch not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<BatchResponse> findById(Integer id, Authentication authentication);
}
