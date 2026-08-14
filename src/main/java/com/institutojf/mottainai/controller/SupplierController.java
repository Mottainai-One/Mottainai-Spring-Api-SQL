package com.institutojf.mottainai.controller;

import com.institutojf.mottainai.controller.swagger.SupplierControllerApi;
import com.institutojf.mottainai.dto.request.CreateSupplierRequest;
import com.institutojf.mottainai.dto.request.UpdateSupplierRequest;
import com.institutojf.mottainai.dto.response.SupplierResponse;
import com.institutojf.mottainai.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/suppliers")
public class SupplierController implements SupplierControllerApi {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @Override
    @PostMapping
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody CreateSupplierRequest request) {
        SupplierResponse supplier = supplierService.create(request);
        URI location = URI.create("/api/v1/suppliers/" + supplier.id());
        return ResponseEntity.created(location).body(supplier);
    }

    @GetMapping
    public ResponseEntity<Page<SupplierResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(supplierService.findAll(pageable));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(supplierService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> update(@PathVariable Integer id, @Valid @RequestBody UpdateSupplierRequest request) {
        return ResponseEntity.ok(supplierService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Integer id) {
        supplierService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
