package com.institutojf.mottainai.service;

import com.institutojf.mottainai.exception.BusinessException;
import com.institutojf.mottainai.model.Promotion;
import com.institutojf.mottainai.model.RetailStore;
import com.institutojf.mottainai.repository.PromotionRepository;
import com.institutojf.mottainai.repository.RetailStoreRepository;
import com.institutojf.mottainai.security.InventoryAccess;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private RetailStoreRepository retailStoreRepository;

    @Mock
    private InventoryAccess inventoryAccess;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PromotionService service;

    @Test
    void shouldRejectUpdateForPromotionFromAnotherStore() {
        Promotion promotion = promotionInStore(10);
        when(promotionRepository.findById(7)).thenReturn(Optional.of(promotion));
        org.mockito.Mockito.doThrow(new BusinessException("User cannot access this store"))
                .when(inventoryAccess).checkStoreAccess(authentication, 10);

        assertThrows(BusinessException.class, () -> service.getPromotionById(7, authentication));

        verify(inventoryAccess).checkStoreAccess(authentication, 10);
    }

    private Promotion promotionInStore(Integer storeId) {
        RetailStore store = new RetailStore();
        store.setId(storeId);
        Promotion promotion = new Promotion();
        promotion.setStore(store);
        return promotion;
    }
}
