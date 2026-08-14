package com.institutojf.mottainai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.institutojf.mottainai.dto.request.CreateSupplierProductRequest;
import com.institutojf.mottainai.dto.response.SupplierProductResponse;
import com.institutojf.mottainai.handler.GlobalExceptionHandler;
import com.institutojf.mottainai.service.SupplierProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SupplierProductControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SupplierProductService supplierProductService;

    @InjectMocks
    private SupplierProductController supplierProductController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(supplierProductController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should create supplier product link with location header")
    void shouldCreateSupplierProductLinkWithLocationHeader() throws Exception {
        when(supplierProductService.create(any())).thenReturn(response());

        mockMvc.perform(post("/api/v1/supplier-products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/supplier-products/1"))
                .andExpect(jsonPath("$.supplierCode").value("FORN-001"));

        verify(supplierProductService).create(any());
    }

    @Test
    @DisplayName("Should reject invalid supplier product request")
    void shouldRejectInvalidSupplierProductRequest() throws Exception {
        mockMvc.perform(post("/api/v1/supplier-products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"supplierId\":1,\"productId\":1,\"purchasePrice\":-1,\"leadTime\":3}"))
                .andExpect(status().isBadRequest());

        verify(supplierProductService, never()).create(any());
    }

    @Test
    @DisplayName("Should deactivate supplier product link with no content response")
    void shouldDeactivateSupplierProductLinkWithNoContentResponse() throws Exception {
        mockMvc.perform(delete("/api/v1/supplier-products/1"))
                .andExpect(status().isNoContent());

        verify(supplierProductService).deactivate(1);
    }

    private CreateSupplierProductRequest request() {
        return new CreateSupplierProductRequest(1, 1, "FORN-001", new BigDecimal("10.00"), 3);
    }

    private SupplierProductResponse response() {
        return new SupplierProductResponse(1, 1, "Fornecedor Teste", 1, "Rice", "FORN-001", new BigDecimal("10.00"), 3, true);
    }
}
