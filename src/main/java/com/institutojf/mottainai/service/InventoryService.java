package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.CreateInventoryRequest;
import com.institutojf.mottainai.dto.request.UpdateInventoryRequest;
import com.institutojf.mottainai.dto.response.InventoryResponse;
import com.institutojf.mottainai.exception.BusinessException;
import com.institutojf.mottainai.exception.ConflictException;
import com.institutojf.mottainai.exception.ResourceNotFoundException;
import com.institutojf.mottainai.mapper.InventoryMapper;
import com.institutojf.mottainai.model.Batch;
import com.institutojf.mottainai.model.Inventory;
import com.institutojf.mottainai.model.enums.InventoryType;
import com.institutojf.mottainai.model.RetailStore;
import com.institutojf.mottainai.repository.BatchRepository;
import com.institutojf.mottainai.repository.InventoryRepository;
import com.institutojf.mottainai.repository.RetailStoreRepository;
import com.institutojf.mottainai.security.InventoryAccess;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final BatchRepository batchRepository;
    private final RetailStoreRepository retailStoreRepository;
    private final InventoryMapper inventoryMapper;
    private final InventoryAccess inventoryAccess;

    public InventoryService(InventoryRepository inventoryRepository, BatchRepository batchRepository, RetailStoreRepository retailStoreRepository, InventoryMapper inventoryMapper, InventoryAccess inventoryAccess) {
        this.inventoryRepository = inventoryRepository;
        this.batchRepository = batchRepository;
        this.retailStoreRepository = retailStoreRepository;
        this.inventoryMapper = inventoryMapper;
        this.inventoryAccess = inventoryAccess;
    }

    @Transactional
    public InventoryResponse create(CreateInventoryRequest request, Authentication authentication) {
        Integer storeId = inventoryAccess.resolveStoreId(authentication, request.storeId());
        InventoryType type = request.inventoryType() == null ? InventoryType.NORMAL : request.inventoryType();
        validateQuantityRange(request.minimumQuantity(), request.maximumQuantity());
        if (inventoryRepository.existsByStore_IdAndBatch_IdAndInventoryTypeAndDeletedAtIsNull(
                storeId, request.batchId(), type)) {
            throw new ConflictException("Inventory already exists");
        }

        RetailStore store = retailStoreRepository.findByIdAndActiveTrueAndDeletedAtIsNull(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));
        Batch batch = batchRepository.findByIdAndActiveTrueAndDeletedAtIsNull(request.batchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

        Inventory inventory = new Inventory();
        inventory.setStore(store);
        inventory.setBatch(batch);
        inventory.setInventoryType(type);
        inventory.setMinimumQuantity(request.minimumQuantity());
        inventory.setMaximumQuantity(request.maximumQuantity());
        inventory.setLocation(request.location());
        return inventoryMapper.toResponse(inventoryRepository.save(inventory));
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> findAll(Integer requestedStoreId, Authentication authentication) {
        return inventoryRepository.findAllByStore_IdAndActiveTrueAndDeletedAtIsNull(
                        inventoryAccess.resolveStoreId(authentication, requestedStoreId))
                .stream()
                .map(inventoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventoryResponse findById(Integer id, Authentication authentication) {
        return inventoryMapper.toResponse(findAccessibleInventory(id, authentication));
    }

    @Transactional
    public InventoryResponse update(Integer id, UpdateInventoryRequest request, Authentication authentication) {
        validateQuantityRange(request.minimumQuantity(), request.maximumQuantity());
        Inventory inventory = findAccessibleInventory(id, authentication);
        inventory.setMinimumQuantity(request.minimumQuantity());
        inventory.setMaximumQuantity(request.maximumQuantity());
        inventory.setLocation(request.location());
        return inventoryMapper.toResponse(inventoryRepository.save(inventory));
    }

    @Transactional
    public void deactivate(Integer id, Authentication authentication) {
        Inventory inventory = findAccessibleInventory(id, authentication);
        inventory.setActive(false);
        inventoryRepository.save(inventory);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> findByBarcode(String barcode, Integer requestedStoreId, Authentication authentication) {
        Integer storeId = inventoryAccess.resolveStoreId(authentication, requestedStoreId);
        return inventoryRepository.findAllByStore_IdAndBatch_Product_BarcodeAndActiveTrueAndDeletedAtIsNull(storeId, barcode)
                .stream()
                .map(inventoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> findExpiring(Integer requestedStoreId, int days, Authentication authentication) {
        Integer storeId = inventoryAccess.resolveStoreId(authentication, requestedStoreId);
        LocalDate today = LocalDate.now();
        return inventoryRepository.findAllByStore_IdAndBatch_ExpirationDateBetweenAndActiveTrueAndDeletedAtIsNull(
                        storeId, today, today.plusDays(days))
                .stream()
                .map(inventoryMapper::toResponse)
                .toList();
    }

    private Inventory findAccessibleInventory(Integer id, Authentication authentication) {
        Inventory inventory = inventoryRepository.findByIdAndActiveTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
        inventoryAccess.checkStoreAccess(authentication, inventory.getStore().getId());
        return inventory;
    }

    private void validateQuantityRange(java.math.BigDecimal minimum, java.math.BigDecimal maximum) {
        if (maximum != null && maximum.compareTo(minimum) < 0) {
            throw new BusinessException("Maximum quantity cannot be below minimum quantity");
        }
    }
}
