package com.institutojf.mottainai.controller;

import com.institutojf.mottainai.controller.swagger.AuthenticationControllerApi;
import com.institutojf.mottainai.dto.request.ForgotPasswordRequest;
import com.institutojf.mottainai.dto.request.LoginRequest;
import com.institutojf.mottainai.dto.request.ResetPasswordRequest;
import com.institutojf.mottainai.dto.response.TokenResponse;
import com.institutojf.mottainai.dto.response.UserResponse;
import com.institutojf.mottainai.service.AuthenticationService;
import com.institutojf.mottainai.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController implements AuthenticationControllerApi {

    private final AuthenticationService authenticationService;
    private final UserProfileService userProfileService;

    public AuthenticationController(AuthenticationService authenticationService, UserProfileService userProfileService) {
        this.authenticationService = authenticationService;
        this.userProfileService = userProfileService;
    }

    @Override
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> profile(Authentication authentication) {
        return ResponseEntity.ok(userProfileService.me(authentication.getName()));
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    // Solicita o envio do código de recuperação para o email informado
    @Override
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authenticationService.requestPasswordReset(request);
        return ResponseEntity.noContent().build();
    }

    // Recebe o código e a nova senha para finalizar a recuperação
    @Override
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}
