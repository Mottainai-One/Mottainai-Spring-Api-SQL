package com.institutojf.mottainai.controller.swagger;

import com.institutojf.mottainai.dto.request.CreateSupplierRequest;
import com.institutojf.mottainai.dto.response.SupplierResponse;
import com.institutojf.mottainai.handler.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Suppliers", description = "API for managing suppliers")
public interface SupplierControllerApi {

    @Operation(summary = "Create a supplier")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Supplier created", content = @Content(schema = @Schema(implementation = SupplierResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Address not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "CNPJ already exists", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<SupplierResponse> create(CreateSupplierRequest request);

    @Operation(summary = "Find a supplier by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supplier found", content = @Content(schema = @Schema(implementation = SupplierResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supplier not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<SupplierResponse> findById(Integer id);
}
