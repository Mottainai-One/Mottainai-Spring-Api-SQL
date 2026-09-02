package com.institutojf.mottainai.controller;

import com.institutojf.mottainai.dto.response.UserResponse;
import com.institutojf.mottainai.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileControllerTest {

    @Mock
    UserProfileService service;

    @InjectMocks
    UserProfileController controller;

    @Test
    void shouldDeriveProfileEmailFromAuthentication() {
        var authentication = new TestingAuthenticationToken("user@test.com", null);
        var response = new UserResponse(1, "User", "12345678901", "user@test.com", null, "OPERATOR", true, 7, null);
        when(service.me("user@test.com")).thenReturn(response);

        var result = controller.me(authentication);

        assertEquals(response, result.getBody());
    }
}
