package com.institutojf.mottainai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.institutojf.mottainai.service.AuthenticationService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController).build();
    }

    @Test
    @DisplayName("Should accept a password recovery request")
    void shouldAcceptAPasswordRecoveryRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EmailRequest("user@mottainai.com"))))
                .andExpect(status().isNoContent());

        verify(authenticationService).requestPasswordReset(any());
    }

    @Test
    @DisplayName("Should accept a valid password reset")
    void shouldAcceptAValidPasswordReset() throws Exception {
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@mottainai.com",
                                  "code": "123456",
                                  "newPassword": "new-password"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(authenticationService).resetPassword(any());
    }

    private record EmailRequest(String email) {
    }
}
