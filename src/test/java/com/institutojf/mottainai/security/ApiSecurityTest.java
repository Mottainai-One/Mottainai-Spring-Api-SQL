package com.institutojf.mottainai.security;

import com.institutojf.mottainai.controller.ProductController;
import com.institutojf.mottainai.dto.response.ProductResponse;
import com.institutojf.mottainai.repository.AppUserRepository;
import com.institutojf.mottainai.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, DatabaseUserDetailsService.class})
@TestPropertySource(properties = {
        "security.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "security.jwt.issuer=https://mottainai.local",
        "security.jwt.expiration-minutes=60"
})
class ApiSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private AppUserRepository appUserRepository;

    @Test
    @DisplayName("Should reject unauthenticated API request")
    void shouldRejectUnauthenticatedApiRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());

        verify(productService, never()).findAll(any());
    }

    @Test
    @DisplayName("Should forbid write operation for operator")
    void shouldForbidWriteOperationForOperator() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .with(user("operator@mottainai.com").roles("OPERATOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":1,\"barcode\":\"7891234567890\",\"name\":\"Rice\",\"unitMeasure\":\"KG\"}"))
                .andExpect(status().isForbidden());

        verify(productService, never()).create(any());
    }

    @Test
    @DisplayName("Should allow write operation for manager")
    void shouldAllowWriteOperationForManager() throws Exception {
        when(productService.create(any())).thenReturn(new ProductResponse(
                1, 1, "Food", "7891234567890", "Rice", null, null, "KG", BigDecimal.ONE, true, 1
        ));

        mockMvc.perform(post("/api/v1/products")
                        .with(user("manager@mottainai.com").roles("MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":1,\"barcode\":\"7891234567890\",\"name\":\"Rice\",\"unitMeasure\":\"KG\"}"))
                .andExpect(status().isCreated());

        verify(productService).create(any());
    }
}
