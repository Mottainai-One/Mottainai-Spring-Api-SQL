package com.institutojf.mottainai.controller;

import com.institutojf.mottainai.controller.swagger.CustomerAuthenticationControllerApi;
import com.institutojf.mottainai.dto.request.CustomerLoginRequest;
import com.institutojf.mottainai.dto.request.CustomerPasswordRecoveryRequest;
import com.institutojf.mottainai.dto.request.CustomerPasswordResetRequest;
import com.institutojf.mottainai.dto.request.CustomerResetTokenRequest;
import com.institutojf.mottainai.dto.response.CustomerTokenResponse;
import com.institutojf.mottainai.dto.response.TokenValidationResponse;
import com.institutojf.mottainai.service.CustomerAuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers/auth")
@RequiredArgsConstructor
public class CustomerAuthenticationController implements CustomerAuthenticationControllerApi {

    private final CustomerAuthenticationService authenticationService;

    @Override
    @PostMapping("/login")
    public CustomerTokenResponse login(@Valid @RequestBody CustomerLoginRequest request) {
        return authenticationService.login(request);
    }

    @Override
    @PostMapping("/password-recovery")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void passwordRecovery(@Valid @RequestBody CustomerPasswordRecoveryRequest request) {
        authenticationService.requestPasswordReset(request);
    }

    @Override
    @PostMapping("/password-reset/validate")
    public TokenValidationResponse validate(@Valid @RequestBody CustomerResetTokenRequest request) {
        return new TokenValidationResponse(authenticationService.validateResetToken(request.token()));
    }

    @Override
    @PostMapping("/password-reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset(@Valid @RequestBody CustomerPasswordResetRequest request) {
        authenticationService.resetPassword(request);
    }

}
