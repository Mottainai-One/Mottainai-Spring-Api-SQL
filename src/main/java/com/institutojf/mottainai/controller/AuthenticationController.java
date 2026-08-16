package com.institutojf.mottainai.controller;

import com.institutojf.mottainai.dto.request.ForgotPasswordRequest;
import com.institutojf.mottainai.dto.request.LoginRequest;
import com.institutojf.mottainai.dto.request.ResetPasswordRequest;
import com.institutojf.mottainai.dto.response.TokenResponse;
import com.institutojf.mottainai.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    // Solicita o envio do código de recuperação para o email informado
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authenticationService.requestPasswordReset(request);
        return ResponseEntity.noContent().build();
    }

    // Recebe o código e a nova senha para finalizar a recuperação
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}
