package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.CreateCustomerRequest;
import com.institutojf.mottainai.exception.BusinessException;
import com.institutojf.mottainai.model.*;
import com.institutojf.mottainai.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerAuthRepository customerAuthRepository;

    @Mock
    private CustomerGeofenceRepository geofenceRepository;

    @Mock
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Mock
    private RetailStoreRepository retailStoreRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerService service;

    @Test
    @DisplayName("Should create customer authentication and loyalty account atomically")
    void createsCustomerAuthenticationAndLoyaltyAccountAtomically() {
        CreateCustomerRequest request = new CreateCustomerRequest("Ana", "12345678909", "ana@example.com", "Strong1!", null, null, true, null);
        when(customerAuthRepository.findByLoginEmailIgnoreCase(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("hash");
        when(customerRepository.save(any())).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.setId(7);
            return customer;
        });
        assertEquals(7, service.create(request).id());
        verify(customerAuthRepository).save(argThat(auth -> auth.getCustomer().getId().equals(7) && auth.getPasswordHash().equals("hash") && auth.getActive()));
        verify(loyaltyAccountRepository)
            .save(argThat(account -> account.getPointsBalance() == 0 && account.getCustomer().getId().equals(7)));
    }

    @Test
    void rejectsWeakPasswordBeforeWritingCustomer() {
        CreateCustomerRequest request = new CreateCustomerRequest("Ana", "12345678909", "ana@example.com", "weakpass", null, null, false, null);
        assertThrows(BusinessException.class, () -> service.create(request));
        verifyNoInteractions(customerRepository);
    }

}
