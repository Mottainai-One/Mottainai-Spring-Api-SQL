package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.response.BatchResponse;
import com.institutojf.mottainai.mapper.BatchMapper;
import com.institutojf.mottainai.model.Batch;
import com.institutojf.mottainai.model.Product;
import com.institutojf.mottainai.repository.BatchRepository;
import com.institutojf.mottainai.repository.ProductRepository;
import com.institutojf.mottainai.security.InventoryAccess;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchServiceTest {

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BatchMapper batchMapper;

    @Mock
    private InventoryAccess inventoryAccess;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BatchService batchService;

    @Test
    @DisplayName("Should change batch status without changing batch data")
    void shouldChangeBatchStatusWithoutChangingBatchData() {
        Batch batch = batch();
        when(batchRepository.findByIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(batch));
        when(batchRepository.save(batch)).thenReturn(batch);
        when(batchMapper.toResponse(batch)).thenReturn(response());
        batchService.updateStatus(1, false, authentication);
        assertFalse(batch.getActive());
        verify(inventoryAccess).requireAdministrator(authentication);
        verify(batchRepository).save(batch);
    }

    private Batch batch() {
        Product product = new Product();
        product.setId(1);
        Batch batch = new Batch();
        batch.setId(1);
        batch.setProduct(product);
        batch.setBatchCode("LOT-001");
        batch.setExpirationDate(LocalDate.now().plusDays(30));
        batch.setInitialQuantity(BigDecimal.ONE);
        batch.setUnitCost(BigDecimal.TEN);
        batch.setActive(true);
        return batch;
    }

    private BatchResponse response() {
        return new BatchResponse(1, 1, "LOT-001", null, LocalDate.now().plusDays(30), BigDecimal.ONE, BigDecimal.TEN,
                false, 1);
    }

}
