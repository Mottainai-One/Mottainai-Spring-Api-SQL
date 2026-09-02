package com.institutojf.mottainai.controller;

import com.institutojf.mottainai.controller.swagger.BatchControllerApi;

import com.institutojf.mottainai.dto.request.CreateBatchRequest;
import com.institutojf.mottainai.dto.response.BatchResponse;
import com.institutojf.mottainai.service.BatchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/batches")
public class BatchController implements BatchControllerApi {
    private final BatchService batchService;

    public BatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    @Override
    @PostMapping
    public ResponseEntity<BatchResponse> create(@Valid @RequestBody CreateBatchRequest request, Authentication authentication) {
        BatchResponse batch = batchService.create(request, authentication);
        return ResponseEntity.created(URI.create("/api/v1/batches/" + batch.id())).body(batch);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<BatchResponse>> findAll(Authentication authentication) {
        return ResponseEntity.ok(batchService.findAll(authentication));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<BatchResponse> findById(@PathVariable Integer id, Authentication authentication) {
        return ResponseEntity.ok(batchService.findById(id, authentication));
    }
}
