package com.institutojf.mottainai.controller.swagger;

import com.institutojf.mottainai.dto.request.CreateAddressRequest;
import com.institutojf.mottainai.dto.response.AddressResponse;
import com.institutojf.mottainai.handler.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Addresses", description = "API for managing addresses")
public interface AddressControllerApi {

    @Operation(summary = "Create an address")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Address created", content = @Content(schema = @Schema(implementation = AddressResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<AddressResponse> create(CreateAddressRequest request);

    @Operation(summary = "Find an address by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address found", content = @Content(schema = @Schema(implementation = AddressResponse.class))),
            @ApiResponse(responseCode = "404", description = "Address not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<AddressResponse> findById(Integer id);
}
