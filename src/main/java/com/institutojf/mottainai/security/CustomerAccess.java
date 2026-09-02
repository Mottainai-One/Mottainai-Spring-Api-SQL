package com.institutojf.mottainai.security;

import com.institutojf.mottainai.exception.ResourceNotFoundException;
import com.institutojf.mottainai.model.Customer;
import com.institutojf.mottainai.repository.CustomerRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CustomerAccess {
    private final CustomerRepository customerRepository;

    public CustomerAccess(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer currentCustomer(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw new ResourceNotFoundException("Customer not found");
        }

        return customerRepository.findByExternalAuthUidAndActiveTrueAndDeletedAtIsNull(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }
}
