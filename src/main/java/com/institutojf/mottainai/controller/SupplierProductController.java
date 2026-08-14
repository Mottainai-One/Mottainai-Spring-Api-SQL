package com.institutojf.mottainai.controller;

import com.institutojf.mottainai.controller.swagger.SupplierProductControllerApi;
import com.institutojf.mottainai.dto.request.CreateSupplierProductRequest;
import com.institutojf.mottainai.dto.request.UpdateSupplierProductRequest;
import com.institutojf.mottainai.dto.response.SupplierProductResponse;
import com.institutojf.mottainai.service.SupplierProductService;
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
@RequestMapping("/api/v1/supplier-products")
public class SupplierProductController implements SupplierProductControllerApi {

    private final SupplierProductService supplierProductService;

    public SupplierProductController(SupplierProductService supplierProductService) {
        this.supplierProductService = supplierProductService;
    }

    @Override
    @PostMapping
    public ResponseEntity<SupplierProductResponse> create(@Valid @RequestBody CreateSupplierProductRequest request) {
        SupplierProductResponse supplierProduct = supplierProductService.create(request);
        URI location = URI.create("/api/v1/supplier-products/" + supplierProduct.id());
        return ResponseEntity.created(location).body(supplierProduct);
    }

    @GetMapping
    public ResponseEntity<Page<SupplierProductResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(supplierProductService.findAll(pageable));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<SupplierProductResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(supplierProductService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierProductResponse> update(@PathVariable Integer id, @Valid @RequestBody UpdateSupplierProductRequest request) {
        return ResponseEntity.ok(supplierProductService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Integer id) {
        supplierProductService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
