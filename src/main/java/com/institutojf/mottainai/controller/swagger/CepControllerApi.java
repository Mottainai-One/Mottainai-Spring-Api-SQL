package com.institutojf.mottainai.controller.swagger;

import com.institutojf.mottainai.dto.response.CepResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;

@Tag(name = "Cep", description = "API for Cep")
public interface CepControllerApi {

    @Operation(summary = "getCepByZipCode")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response")
    })
    ResponseEntity<CepResponse> getCepByZipCode(@Pattern(regexp = "\\d{8}", message = "CEP must have exactly 8 digits") String cep);

}
