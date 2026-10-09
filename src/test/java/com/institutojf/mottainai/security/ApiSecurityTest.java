package com.institutojf.mottainai.security;

import com.institutojf.mottainai.repository.AppUserRepository;
import com.institutojf.mottainai.repository.CustomerAuthRepository;
import com.institutojf.mottainai.service.RlsContextService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiSecurityTest.SecurityTestController.class)
@Import({ SecurityConfig.class, DatabaseUserDetailsService.class, ApiSecurityTest.SecurityTestController.class })
@TestPropertySource(properties = { "security.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "security.jwt.issuer=https://mottainai.local", "security.jwt.expiration-minutes=60",
        "security.cors.allowed-origins=https://app.example" })
class ApiSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppUserRepository appUserRepository;

    @MockitoBean
    private RlsContextService rlsContextService;

    @MockitoBean
    private CustomerAuthRepository customerAuthRepository;

    @Test
    @DisplayName("Should reject unauthenticated API request")
    void shouldRejectUnauthenticatedApiRequest() throws Exception {
        mockMvc.perform(get("/api/v1/security-probe")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should forbid write operation for operator")
    void shouldForbidWriteOperationForOperator() throws Exception {
        mockMvc.perform(post("/api/v1/security-probe").with(user("operator@mottainai.com").roles("OPERATOR"))).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow write operation for manager")
    void shouldAllowWriteOperationForManager() throws Exception {
        mockMvc.perform(post("/api/v1/security-probe").with(user("manager@mottainai.com").roles("MANAGER"))).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should allow idempotency header in browser preflight")
    void shouldAllowIdempotencyHeaderInPreflight() throws Exception {
        mockMvc
            .perform(options("/api/v1/loyalty/redeem").header("Origin", "https://app.example")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Idempotency-Key,Content-Type"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Headers", containsString("Idempotency-Key")));
    }

    @Test
    @DisplayName("Should forbid PATCH operation for operator")
    void shouldForbidPatchForOperator() throws Exception {
        mockMvc.perform(patch("/api/v1/security-probe").with(user("operator@mottainai.com").roles("OPERATOR"))).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow PATCH operation for manager")
    void shouldAllowPatchForManager() throws Exception {
        mockMvc.perform(patch("/api/v1/security-probe").with(user("manager@mottainai.com").roles("MANAGER"))).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should reject customer access to a staff endpoint")
    void shouldRejectCustomerFromStaffEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/security-probe").with(user("customer@mottainai.com").roles("CUSTOMER"))).andExpect(status().isForbidden());
    }

    @RestController
    @RequestMapping("/api/v1/security-probe")
    static class SecurityTestController {

        @GetMapping
        public String read() {
            return "ok";
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public String create() {
            return "created";
        }

        @PatchMapping
        public String update() {
            return "updated";
        }
    }

}
