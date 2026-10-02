package com.institutojf.mottainai.security;

import com.institutojf.mottainai.service.RlsContextService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Revalida o JWT interno contra a conta e a sessão antes de liberar a requisição.
 */
@RequiredArgsConstructor
public class StaffAccessFilter extends OncePerRequestFilter {
    private final RlsContextService rlsContextService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Bloqueia tokens de refresh, sessões revogadas e permissões desatualizadas
        if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken token
                && !rlsContextService.validateAccessToken(token.getToken())) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        // Prossegue com a autenticação validada pelo banco
        filterChain.doFilter(request, response);
    }
}
