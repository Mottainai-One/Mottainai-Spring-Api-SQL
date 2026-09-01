package com.institutojf.mottainai.security;

import com.institutojf.mottainai.exception.BusinessException;
import com.institutojf.mottainai.exception.ResourceNotFoundException;
import com.institutojf.mottainai.model.AppUser;
import com.institutojf.mottainai.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class InventoryAccess {
    private final AppUserRepository appUserRepository;

    public InventoryAccess(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AppUser currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw new BusinessException("Authenticated user required");
        }
        return appUserRepository.findByEmailIgnoreCaseAndActiveTrueAndDeletedAtIsNull(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public AppUser user(Authentication authentication) {
        return currentUser(authentication);
    }

    public Integer store(Authentication authentication, Integer requestedStoreId) {
        return resolveStoreId(authentication, requestedStoreId);
    }

    public Integer resolveStoreId(Authentication authentication, Integer requestedStoreId) {
        AppUser user = currentUser(authentication);
        Integer userStoreId = user.getEmployee().getStore().getId();
        if (isAdministrator(user)) {
            if (requestedStoreId == null) {
                throw new BusinessException("Store id is required for administrators");
            }
            return requestedStoreId;
        }
        return userStoreId;
    }

    public void checkStoreAccess(Authentication authentication, Integer storeId) {
        Integer permittedStoreId = resolveStoreId(authentication, storeId);
        if (!permittedStoreId.equals(storeId)) {
            throw new BusinessException("User cannot access this store");
        }
    }

    public void requireAdministrator(Authentication authentication) {
        if (!isAdministrator(currentUser(authentication))) {
            throw new BusinessException("Administrator access is required");
        }
    }

    private boolean isAdministrator(AppUser user) {
        return "ADMINISTRATOR".equalsIgnoreCase(user.getEmployee().getRole().getName());
    }
}
