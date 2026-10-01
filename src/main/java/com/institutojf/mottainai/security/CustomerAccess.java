package com.institutojf.mottainai.security;

import com.institutojf.mottainai.exception.ResourceNotFoundException;
import com.institutojf.mottainai.model.Customer;
import com.institutojf.mottainai.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerAccess {

    private final CustomerRepository customerRepository;

    public Customer currentCustomer(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw new ResourceNotFoundException("Customer not found");
        }
        return customerRepository.findByExternalAuthUidAndActiveTrueAndDeletedAtIsNull(authentication.getName())
            .or(() -> customerRepository.findByEmailIgnoreCaseAndActiveTrueAndDeletedAtIsNull(authentication.getName()))
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    public boolean canAccess(Authentication authentication, Integer customerId) {
        try {
            return currentCustomer(authentication).getId().equals(customerId);
        }
        catch (ResourceNotFoundException exception) {
            return false;
        }
    }

}
