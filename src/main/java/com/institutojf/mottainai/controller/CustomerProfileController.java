package com.institutojf.mottainai.controller;

import com.institutojf.mottainai.controller.swagger.CustomerProfileControllerApi;
import com.institutojf.mottainai.dto.response.CustomerResponse;
import com.institutojf.mottainai.service.CustomerAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CustomerProfileController implements CustomerProfileControllerApi {

    private final CustomerAuthenticationService authenticationService;

    @Override
    @GetMapping("/customers/auth/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CustomerResponse profile(Authentication authentication) {
        return authenticationService.profile(authentication);
    }

    @Override
    @GetMapping("/client/auth/profile")
    public CustomerResponse clientProfile(Authentication authentication) {
        return authenticationService.profile(authentication);
    }

}
