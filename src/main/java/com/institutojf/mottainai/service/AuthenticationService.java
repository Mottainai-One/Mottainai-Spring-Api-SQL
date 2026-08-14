package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.LoginRequest;
import com.institutojf.mottainai.dto.response.TokenResponse;
import com.institutojf.mottainai.model.AppUser;
import com.institutojf.mottainai.repository.AppUserRepository;
import com.institutojf.mottainai.security.JwtProperties;
import com.institutojf.mottainai.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            AppUserRepository appUserRepository,
            JwtService jwtService,
            JwtProperties jwtProperties
    ) {
        this.authenticationManager = authenticationManager;
        this.appUserRepository = appUserRepository;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password())
        );
        AppUser user = appUserRepository.findByEmailIgnoreCaseAndActiveTrueAndDeletedAtIsNull(authentication.getName())
                .orElseThrow();
        user.setLastLogin(LocalDateTime.now());
        appUserRepository.save(user);

        return new TokenResponse(
                jwtService.generateToken(authentication),
                "Bearer",
                jwtProperties.expirationMinutes() * 60
        );
    }
}
