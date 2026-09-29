package com.institutojf.mottainai.security;

import com.institutojf.mottainai.service.RlsContextService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class StaffAccessFilterTest {
    private final RlsContextService rlsContextService = mock(RlsContextService.class);
    private final StaffAccessFilter filter = new StaffAccessFilter(rlsContextService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should reject a staff token that fails session validation")
    void shouldRejectInvalidStaffSession() throws Exception {
        Jwt jwt = token();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(new MockHttpServletRequest(), response, chain);

        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(rlsContextService).validateAccessToken(jwt);
        verifyNoInteractions(chain);
    }

    @Test
    @DisplayName("Should pass a staff token with a valid session")
    void shouldAllowValidStaffSession() throws Exception {
        Jwt jwt = token();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        when(rlsContextService.validateAccessToken(jwt)).thenReturn(true);
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    private Jwt token() {
        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "HS256"), Map.of("sub", "admin@example.com"));
    }
}
