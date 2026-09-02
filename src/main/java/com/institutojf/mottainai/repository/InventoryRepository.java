package com.institutojf.mottainai.repository;

import com.institutojf.mottainai.model.Inventory;
import com.institutojf.mottainai.model.enums.InventoryType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.id = :id and i.active = true and i.deletedAt is null")
    Optional<Inventory> findActiveByIdForUpdate(@Param("id") Integer id);

    Optional<Inventory> findByIdAndActiveTrueAndDeletedAtIsNull(Integer id);

    /**
     * Lista os registros de inventário ativos e não excluídos de uma loja
     */
    List<Inventory> findAllByStore_IdAndActiveTrueAndDeletedAtIsNull(Integer storeId);

    /**
     * Lista os registros de inventário ativos e não excluídos de uma loja pelo código de barras do produto do lote
     */
    List<Inventory> findAllByStore_IdAndBatch_Product_BarcodeAndActiveTrueAndDeletedAtIsNull(Integer storeId, String barcode);

    /**
     * Lista os registros de inventário ativos e não excluídos de uma loja em que a validade do lote esteja no intervalo informado
     */
    List<Inventory> findAllByStore_IdAndBatch_ExpirationDateBetweenAndActiveTrueAndDeletedAtIsNull(Integer storeId, LocalDate startDate, LocalDate endDate);

    /**
     * Verifica se já existe um registro de inventário não excluído para a combinação de loja, lote e tipo
     */
    boolean existsByStore_IdAndBatch_IdAndInventoryTypeAndDeletedAtIsNull(Integer storeId, Integer batchId, InventoryType inventoryType);
}
