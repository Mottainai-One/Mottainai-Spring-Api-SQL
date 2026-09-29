package com.institutojf.mottainai.security;

import com.institutojf.mottainai.exception.ResourceNotFoundException;
import com.institutojf.mottainai.model.Customer;
import com.institutojf.mottainai.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerAccessTest {
    @Mock
    private CustomerRepository customerRepository;

    @Test
    @DisplayName("Should resolve active customer from firebase subject")
    void shouldResolveActiveCustomerFromFirebaseSubject() {
        Customer customer = new Customer();
        customer.setId(7);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("firebase-uid", null);
        authentication.setAuthenticated(true);
        when(customerRepository.findByExternalAuthUidAndActiveTrueAndDeletedAtIsNull("firebase-uid"))
                .thenReturn(Optional.of(customer));

        Customer result = new CustomerAccess(customerRepository).currentCustomer(authentication);

        assertEquals(7, result.getId());
    }

    @Test
    @DisplayName("Should reject unknown firebase subject")
    void shouldRejectUnknownFirebaseSubject() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("firebase-uid", null);
        authentication.setAuthenticated(true);
        when(customerRepository.findByExternalAuthUidAndActiveTrueAndDeletedAtIsNull("firebase-uid"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> new CustomerAccess(customerRepository).currentCustomer(authentication));
    }
}
