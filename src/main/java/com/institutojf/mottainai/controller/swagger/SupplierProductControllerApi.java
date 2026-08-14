package com.institutojf.mottainai.controller.swagger;

import com.institutojf.mottainai.dto.request.CreateSupplierProductRequest;
import com.institutojf.mottainai.dto.response.SupplierProductResponse;
import com.institutojf.mottainai.handler.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Supplier Products", description = "API for managing supplier-product links")
public interface SupplierProductControllerApi {

    @Operation(summary = "Link a supplier to a product")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Supplier-product link created", content = @Content(schema = @Schema(implementation = SupplierProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Supplier or product not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Supplier is already linked to the product", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<SupplierProductResponse> create(CreateSupplierProductRequest request);

    @Operation(summary = "Find a supplier-product link by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supplier-product link found", content = @Content(schema = @Schema(implementation = SupplierProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supplier-product link not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<SupplierProductResponse> findById(Integer id);
}
