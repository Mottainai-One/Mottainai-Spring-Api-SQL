package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.CreateBatchRequest;
import com.institutojf.mottainai.dto.response.BatchResponse;
import com.institutojf.mottainai.exception.BusinessException;
import com.institutojf.mottainai.exception.ConflictException;
import com.institutojf.mottainai.exception.ResourceNotFoundException;
import com.institutojf.mottainai.mapper.BatchMapper;
import com.institutojf.mottainai.model.Batch;
import com.institutojf.mottainai.model.Product;
import com.institutojf.mottainai.repository.BatchRepository;
import com.institutojf.mottainai.repository.ProductRepository;
import com.institutojf.mottainai.security.InventoryAccess;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BatchService {
    private final BatchRepository batchRepository;
    private final ProductRepository productRepository;
    private final BatchMapper batchMapper;
    private final InventoryAccess inventoryAccess;

    public BatchService(BatchRepository batchRepository, ProductRepository productRepository, BatchMapper batchMapper, InventoryAccess inventoryAccess) {
        this.batchRepository = batchRepository;
        this.productRepository = productRepository;
        this.batchMapper = batchMapper;
        this.inventoryAccess = inventoryAccess;
    }

    @Transactional
    public BatchResponse create(CreateBatchRequest request, Authentication authentication) {
        inventoryAccess.requireAdministrator(authentication);
        if (batchRepository.existsByBatchCode(request.batchCode())) {
            throw new ConflictException("Batch code already exists");
        }
        if (request.manufactureDate() != null && request.manufactureDate().isAfter(request.expirationDate())) {
            throw new BusinessException("Manufacture date cannot be after expiration date");
        }
        if (request.expirationDate().isBefore(java.time.LocalDate.now())) {
            throw new BusinessException("Expiration date cannot be in the past");
        }
        Product product = productRepository.findByIdAndActiveTrueAndDeletedAtIsNull(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        Batch batch = new Batch();
        batch.setProduct(product);
        batch.setReceivingItemId(request.receivingItemId());
        batch.setBatchCode(request.batchCode());
        batch.setManufactureDate(request.manufactureDate());
        batch.setExpirationDate(request.expirationDate());
        batch.setInitialQuantity(request.initialQuantity());
        batch.setUnitCost(request.unitCost());
        return batchMapper.toResponse(batchRepository.save(batch));
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> findAll(Authentication authentication) {
        inventoryAccess.requireAdministrator(authentication);
        return batchRepository.findAllByActiveTrueAndDeletedAtIsNull().stream().map(batchMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BatchResponse findById(Integer id, Authentication authentication) {
        inventoryAccess.requireAdministrator(authentication);
        Batch batch = batchRepository.findByIdAndActiveTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));
        return batchMapper.toResponse(batch);
    }
}
