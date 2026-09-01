package com.institutojf.mottainai.controller;

import com.institutojf.mottainai.controller.swagger.RetailStoreControllerApi;
import com.institutojf.mottainai.dto.request.CreateRetailStoreRequest;
import com.institutojf.mottainai.dto.request.UpdateRetailStoreRequest;
import com.institutojf.mottainai.dto.response.RetailStoreResponse;
import com.institutojf.mottainai.service.RetailStoreService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/stores")
public class RetailStoreController implements RetailStoreControllerApi {

    private final RetailStoreService retailStoreService;

    public RetailStoreController(RetailStoreService retailStoreService) {
        this.retailStoreService = retailStoreService;
    }

    @Override
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<RetailStoreResponse> create(@Valid @RequestBody CreateRetailStoreRequest request) {
        RetailStoreResponse store = retailStoreService.create(request);
        URI location = URI.create("/api/v1/stores/" + store.id());
        return ResponseEntity.created(location).body(store);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Page<RetailStoreResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(retailStoreService.findAll(pageable));
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or (hasRole('MANAGER') and @retailStoreAccess.isCurrentUserStore(#id, authentication))")
    public ResponseEntity<RetailStoreResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(retailStoreService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or (hasRole('MANAGER') and @retailStoreAccess.isCurrentUserStore(#id, authentication))")
    public ResponseEntity<RetailStoreResponse> update(@PathVariable Integer id, @Valid @RequestBody UpdateRetailStoreRequest request) {
        return ResponseEntity.ok(retailStoreService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deactivate(@PathVariable Integer id) {
        retailStoreService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
