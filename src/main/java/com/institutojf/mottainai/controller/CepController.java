package com.institutojf.mottainai.controller;

import com.institutojf.mottainai.dto.response.CepResponse;
import com.institutojf.mottainai.service.BrasilApiService;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cep")
public class CepController {

    private final BrasilApiService brasilApiService;

    public CepController(BrasilApiService brasilApiService) {
        this.brasilApiService = brasilApiService;
    }

    @GetMapping("/{cep}")
    public ResponseEntity<CepResponse> getCepByZipCode(@PathVariable @Pattern(regexp = "\\d{8}", message = "CEP must have exactly 8 digits") String cep) {
        CepResponse response = brasilApiService.getCep(cep);
        return ResponseEntity.ok(response);
    }
}