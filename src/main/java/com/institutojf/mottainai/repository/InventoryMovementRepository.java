package com.institutojf.mottainai.repository;

import com.institutojf.mottainai.model.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Integer> {
    List<InventoryMovement> findAllByInventory_IdOrderByMovementDateDesc(Integer inventoryId);
}
