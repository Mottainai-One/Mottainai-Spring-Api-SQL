package com.institutojf.mottainai.controller;

import com.institutojf.mottainai.controller.swagger.UserProfileControllerApi;

import com.institutojf.mottainai.dto.request.InviteStoreUserRequest;
import com.institutojf.mottainai.dto.request.UpdateStoreUserRequest;
import com.institutojf.mottainai.dto.response.InviteStoreUserResponse;
import com.institutojf.mottainai.dto.response.RetailStoreResponse;
import com.institutojf.mottainai.dto.response.UserResponse;
import com.institutojf.mottainai.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UserProfileController implements UserProfileControllerApi {
    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Override
    @GetMapping("/users/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        return ResponseEntity.ok(userProfileService.me(authentication.getName()));
    }

    @Override
    @GetMapping("/stores/me")
    public ResponseEntity<RetailStoreResponse> myStore(Authentication authentication) {
        return ResponseEntity.ok(userProfileService.myStore(authentication.getName()));
    }

    @Override
    @GetMapping("/store-users")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userProfileService.findAll());
    }

    @Override
    @GetMapping("/store-users/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<UserResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(userProfileService.findById(id));
    }

    @Override
    @PostMapping("/store-users/invite")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<InviteStoreUserResponse> invite(@Valid @RequestBody InviteStoreUserRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userProfileService.invite(request, authentication.getName()));
    }

    @Override
    @PatchMapping("/store-users/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<UserResponse> update(@PathVariable Integer id, @Valid @RequestBody UpdateStoreUserRequest request) {
        return ResponseEntity.ok(userProfileService.update(id, request));
    }
}
