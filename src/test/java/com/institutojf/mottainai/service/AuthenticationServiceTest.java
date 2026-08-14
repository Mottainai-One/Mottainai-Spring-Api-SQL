package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.LoginRequest;
import com.institutojf.mottainai.dto.response.TokenResponse;
import com.institutojf.mottainai.model.AppUser;
import com.institutojf.mottainai.repository.AppUserRepository;
import com.institutojf.mottainai.security.JwtProperties;
import com.institutojf.mottainai.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("Should return bearer token and update last login after authentication")
    void shouldReturnBearerTokenAndUpdateLastLoginAfterAuthentication() {
        LoginRequest request = new LoginRequest("manager@mottainai.com", "password");
        AppUser user = new AppUser();
        user.setEmail(request.email());
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(request.email(), null, "ROLE_MANAGER");
        when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any())).thenReturn(authentication);
        when(appUserRepository.findByEmailIgnoreCaseAndActiveTrueAndDeletedAtIsNull(request.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(authentication)).thenReturn("signed-token");
        when(jwtProperties.expirationMinutes()).thenReturn(60L);

        TokenResponse response = authenticationService.login(request);

        assertEquals("signed-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600, response.expiresIn());
        verify(jwtService).generateToken(authentication);
        verify(appUserRepository).save(user);
    }
}
