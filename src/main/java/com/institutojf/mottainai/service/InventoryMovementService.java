package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.CreateInventoryMovementRequest;
import com.institutojf.mottainai.dto.response.InventoryMovementResponse;
import com.institutojf.mottainai.exception.BusinessException;
import com.institutojf.mottainai.exception.ResourceNotFoundException;
import com.institutojf.mottainai.mapper.InventoryMovementMapper;
import com.institutojf.mottainai.model.Inventory;
import com.institutojf.mottainai.model.InventoryMovement;
import com.institutojf.mottainai.model.enums.MovementType;
import com.institutojf.mottainai.repository.InventoryMovementRepository;
import com.institutojf.mottainai.repository.InventoryRepository;
import com.institutojf.mottainai.security.InventoryAccess;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryMovementService {
    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryMovementMapper inventoryMovementMapper;
    private final InventoryAccess inventoryAccess;

    public InventoryMovementService(
            InventoryRepository inventoryRepository,
            InventoryMovementRepository inventoryMovementRepository,
            InventoryMovementMapper inventoryMovementMapper,
            InventoryAccess inventoryAccess
    ) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.inventoryMovementMapper = inventoryMovementMapper;
        this.inventoryAccess = inventoryAccess;
    }

    @Transactional
    public InventoryMovementResponse create(Integer inventoryId, CreateInventoryMovementRequest request,
                                            Authentication authentication) {
        Inventory inventory = inventoryRepository.findActiveByIdForUpdate(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
        inventoryAccess.checkStoreAccess(authentication, inventory.getStore().getId());
        validateDirection(request.movementType(), request.movedQuantity());

        BigDecimal previousBalance = inventory.getCurrentQuantity();
        BigDecimal currentBalance = previousBalance.add(request.movedQuantity());
        if (currentBalance.signum() < 0) {
            throw new BusinessException("Insufficient inventory balance");
        }

        inventory.setCurrentQuantity(currentBalance);
        InventoryMovement movement = new InventoryMovement();
        movement.setInventory(inventory);
        movement.setEmployee(inventoryAccess.currentUser(authentication).getEmployee());
        movement.setMovementDate(LocalDateTime.now());
        movement.setMovementType(request.movementType());
        movement.setMovedQuantity(request.movedQuantity());
        movement.setPreviousBalance(previousBalance);
        movement.setCurrentBalance(currentBalance);
        movement.setObservation(request.observation());
        movement.setStoreId(inventory.getStore().getId());

        inventoryRepository.save(inventory);
        return inventoryMovementMapper.toResponse(inventoryMovementRepository.save(movement));
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> findByInventory(Integer inventoryId, Authentication authentication) {
        Inventory inventory = inventoryRepository.findByIdAndActiveTrueAndDeletedAtIsNull(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
        inventoryAccess.checkStoreAccess(authentication, inventory.getStore().getId());
        return inventoryMovementRepository.findAllByInventory_IdOrderByMovementDateDesc(inventoryId)
                .stream()
                .map(inventoryMovementMapper::toResponse)
                .toList();
    }

    private void validateDirection(MovementType type, BigDecimal quantity) {
        if (quantity.signum() == 0) {
            throw new BusinessException("Movement quantity cannot be zero");
        }
        switch (type) {
            case IN:
                if (quantity.signum() < 0) {
                    throw new BusinessException("IN movements must have positive quantity");
                }
                break;
            case OUT:
            case DISPOSAL:
                if (quantity.signum() > 0) {
                    throw new BusinessException("OUT and DISPOSAL movements must have negative quantity");
                }
                break;
            case ADJUSTMENT:
            case TRANSFER:
            case DONATION:
                break;
        }
    }
}
