package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.UpdateInventoryRequest;
import com.institutojf.mottainai.exception.BusinessException;
import com.institutojf.mottainai.mapper.InventoryMapper;
import com.institutojf.mottainai.model.Inventory;
import com.institutojf.mottainai.repository.BatchRepository;
import com.institutojf.mottainai.repository.InventoryRepository;
import com.institutojf.mottainai.repository.RetailStoreRepository;
import com.institutojf.mottainai.security.InventoryAccess;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private RetailStoreRepository retailStoreRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private InventoryAccess inventoryAccess;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private InventoryService service;

    @Test
    void shouldUseStoreResolvedFromAuthenticationWhenListing() {
        when(inventoryAccess.resolveStoreId(authentication, 99)).thenReturn(2);
        when(inventoryRepository.findAllByStore_IdAndActiveTrueAndDeletedAtIsNull(2)).thenReturn(List.of());

        service.findAll(99, authentication);

        verify(inventoryRepository).findAllByStore_IdAndActiveTrueAndDeletedAtIsNull(2);
    }

    @Test
    void shouldRejectUpdateWhenMaximumIsBelowMinimum() {
        assertThrows(BusinessException.class, () -> service.update(
                1,
                new UpdateInventoryRequest(new BigDecimal("3.000"), new BigDecimal("2.000"), null),
                authentication
        ));
    }
}
