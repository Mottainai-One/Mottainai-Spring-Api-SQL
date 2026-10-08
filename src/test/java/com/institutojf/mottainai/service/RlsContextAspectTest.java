package com.institutojf.mottainai.service;

import com.institutojf.mottainai.security.JwtProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RlsContextAspectTest {
    private final RlsContextService rlsContextService = mock(RlsContextService.class);
    private final RlsContextAspect aspect = new RlsContextAspect(rlsContextService,
            new JwtProperties("unused", "https://mottainai.local", 60, 7));

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should leave firebase authentication outside staff context")
    void shouldLeaveFirebaseAuthenticationOutsideStaffContext() throws Throwable {
        SecurityContextHolder.getContext().setAuthentication(token("customer-uid",
                "https://securetoken.google.com/customer-project"));
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn("ok");

        assertEquals("ok", aspect.setContext(joinPoint));
        verifyNoInteractions(rlsContextService);
    }

    @Test
    @DisplayName("Should reject staff request without matching database context")
    void shouldRejectStaffRequestWithoutMatchingDatabaseContext() {
        SecurityContextHolder.getContext().setAuthentication(token("admin@example.com",
                "https://mottainai.local"));
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        assertThrows(BadCredentialsException.class, () -> aspect.setContext(joinPoint));
        verify(rlsContextService).bootstrapByEmail("admin@example.com");
    }

    @Test
    @DisplayName("Should set staff RLS context before running a transactional service")
    void shouldBootstrapStaffContextBeforeProceeding() throws Throwable {
        SecurityContextHolder.getContext().setAuthentication(token("admin@example.com",
                "https://mottainai.local"));
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(rlsContextService.bootstrapByEmail("admin@example.com")).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("ok");

        assertEquals("ok", aspect.setContext(joinPoint));

        verify(rlsContextService).bootstrapByEmail("admin@example.com");
        verify(joinPoint).proceed();
    }

    private JwtAuthenticationToken token(String subject, String issuer) {
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "HS256"), Map.of("sub", subject, "iss", issuer));
        return new JwtAuthenticationToken(jwt);
    }
}
