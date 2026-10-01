package com.institutojf.mottainai.security;

import com.institutojf.mottainai.repository.AppUserRepository;
import com.institutojf.mottainai.service.RlsContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("retailStoreAccess")
@RequiredArgsConstructor
public class RetailStoreAccess {

    private final AppUserRepository appUserRepository;

    private final RlsContextService rlsContextService;

    @Transactional(readOnly = true)
    public boolean isCurrentUserStore(Integer storeId, Authentication authentication) {
        if (storeId == null || authentication == null || authentication.getName() == null
                || !rlsContextService.bootstrapByEmail(authentication.getName())) {
            return false;
        }
        return appUserRepository.findByEmailIgnoreCaseAndActiveTrueAndDeletedAtIsNull(authentication.getName())
            .map(user -> storeId.equals(user.getEmployee().getStore().getId()))
            .orElse(false);
    }

}
