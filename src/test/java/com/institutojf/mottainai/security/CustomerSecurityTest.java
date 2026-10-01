package com.institutojf.mottainai.security;

import com.institutojf.mottainai.controller.*;
import com.institutojf.mottainai.dto.response.*;
import com.institutojf.mottainai.repository.AppUserRepository;
import com.institutojf.mottainai.repository.CustomerAuthRepository;
import com.institutojf.mottainai.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ CustomerController.class, CustomerAuthenticationController.class })
@Import({ SecurityConfig.class, DatabaseUserDetailsService.class })
@TestPropertySource(properties = { "security.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "security.jwt.issuer=https://mottainai.local", "security.jwt.expiration-minutes=60" })
class CustomerSecurityTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CustomerService customerService;

    @MockitoBean
    LoyaltyService loyaltyService;

    @MockitoBean
    CustomerAuthenticationService customerAuthenticationService;

    @MockitoBean
    CustomerAccess customerAccess;

    @MockitoBean
    AppUserRepository appUserRepository;

    @MockitoBean
    RlsContextService rlsContextService;

    @MockitoBean
    CustomerAuthRepository customerAuthRepository;

    @Test
    @DisplayName("Should permit customer login without authentication")
    void permitsCustomerLoginWithoutAuthentication() throws Exception {
        when(customerAuthenticationService.login(any())).thenReturn(new CustomerTokenResponse("token", "Bearer", 3600));
        mockMvc
            .perform(post("/api/v1/customers/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@example.com\",\"password\":\"Strong1!\"}"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should block customer from administrative customer list")
    void blocksCustomerFromAdministrativeCustomerList() throws Exception {
        mockMvc.perform(get("/api/v1/customers").with(user("ana@example.com").roles("CUSTOMER")))
            .andExpect(status().isForbidden());
        verifyNoInteractions(customerService);
    }

    @Test
    @DisplayName("Should permit customer to read own record")
    void permitsCustomerToReadOwnRecord() throws Exception {
        when(customerAccess.canAccess(any(), eq(7))).thenReturn(true);
        when(customerService.find(7)).thenReturn(new CustomerResponse(7, "Ana", "***.456.789-**", "ana@example.com",
                null, null, true, false, null, null, null));
        mockMvc.perform(get("/api/v1/customers/7").with(user("ana@example.com").roles("CUSTOMER")))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should permit administrator to list customers")
    void permitsAdministratorToListCustomers() throws Exception {
        when(customerService.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/customers").with(user("admin@example.com").roles("ADMINISTRATOR")))
            .andExpect(status().isOk());
    }

}
