package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.*;
import com.institutojf.mottainai.dto.response.*;
import com.institutojf.mottainai.exception.*;
import com.institutojf.mottainai.model.*;
import com.institutojf.mottainai.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    private final CustomerAuthRepository customerAuthRepository;

    private final CustomerGeofenceRepository geofenceRepository;

    private final LoyaltyAccountRepository loyaltyAccountRepository;

    private final RetailStoreRepository retailStoreRepository;

    private final AddressRepository addressRepository;

    private final AuditLogRepository auditLogRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CustomerResponse create(CreateCustomerRequest request) {
        validatePassword(request.password());

        if (customerRepository.existsByCpfAndDeletedAtIsNull(request.cpf())) {
            throw new ConflictException("CPF is already registered");
        }
        if (customerRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(request.email())
                || customerAuthRepository.findByLoginEmailIgnoreCase(request.email()).isPresent()) {
            throw new ConflictException("Email is already registered");

        }
        LocalDateTime now = LocalDateTime.now();

        Customer customer = new Customer();
        customer.setFullName(request.fullName().trim());
        customer.setCpf(request.cpf());
        customer.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        customer.setPhone(normalize(request.phone()));
        customer.setBirthDate(request.birthDate());
        customer.setMarketingConsent(Boolean.TRUE.equals(request.marketingConsent()));
        customer.setActive(true);
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);

        if (request.address() != null) {
            customer.setAddress(addressRepository.save(toAddress(request.address())));
        }
        customerRepository.save(customer);

        CustomerAuth auth = new CustomerAuth();
        auth.setCustomer(customer);
        auth.setLoginEmail(customer.getEmail());
        auth.setPasswordHash(passwordEncoder.encode(request.password()));
        auth.setPasswordChangedAt(now);
        auth.setCreatedAt(now);
        auth.setUpdatedAt(now);
        customerAuthRepository.save(auth);

        LoyaltyAccount account = new LoyaltyAccount();
        account.setCustomer(customer);
        account.setPointsBalance(0);
        account.setJoinedAt(now);
        account.setActive(true);
        account.setUpdatedAt(now);
        loyaltyAccountRepository.save(account);
        auditLogRepository.record("customer", "INSERT", customer.getId().toString(), null, null,
                Map.of("active", true));
        return CustomerResponse.fromEntity(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return customerRepository.findByDeletedAtIsNullOrderByFullName()
            .stream()
            .map(CustomerResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse find(Integer id) {
        return CustomerResponse.fromEntity(customer(id));
    }

    @Transactional
    public CustomerResponse update(Integer id, UpdateCustomerRequest request) {
        Customer customer = customer(id);
        if (!customer.getEmail().equalsIgnoreCase(request.email()) && customerRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(request.email())) {
            throw new ConflictException("Email is already registered");
        }
        Map<String, Object> oldData = Map.of("email", customer.getEmail(), "marketing_consent", customer.getMarketingConsent());
        customer.setFullName(request.fullName().trim());
        customer.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        customer.setPhone(normalize(request.phone()));
        customer.setBirthDate(request.birthDate());
        customer.setMarketingConsent(request.marketingConsent());
        customer.setUpdatedAt(LocalDateTime.now());

        if (request.address() != null) {
            customer.setAddress(addressRepository.save(toAddress(request.address())));
        }

        customerAuthRepository.findByCustomer_Id(id).ifPresent(auth -> {
            auth.setLoginEmail(customer.getEmail());
            auth.setUpdatedAt(LocalDateTime.now());
            customerAuthRepository.save(auth);
        });
        customerRepository.save(customer);
        auditLogRepository.record("customer", "UPDATE", id.toString(), null, oldData,
                Map.of("email", customer.getEmail(), "marketing_consent", customer.getMarketingConsent()));
        return CustomerResponse.fromEntity(customer);
    }

    @Transactional
    public void delete(Integer id) {
        Customer customer = customer(id);
        customer.setActive(false);
        customer.setDeletedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        customerAuthRepository.findByCustomer_Id(id).ifPresent(auth -> {
            auth.setActive(false);
            customerAuthRepository.save(auth);
        });
        loyaltyAccountRepository.findByCustomer_Id(id).ifPresent(account -> {
            account.setActive(false);
            loyaltyAccountRepository.save(account);
        });
        geofenceRepository.findByCustomer_IdAndActiveTrueOrderByCreatedAtDesc(id)
            .forEach(geofence -> geofence.setActive(false));
        customerRepository.save(customer);
        auditLogRepository.record("customer", "DELETE", id.toString(), null, Map.of("active", true), Map.of("active", false));
    }

    @Transactional
    public void anonymize(Integer id) {
        Customer customer = customer(id);
        String marker = UUID.randomUUID().toString();
        customer.setFullName("ANONYMIZED-" + marker);
        customer.setCpf(null);
        customer.setEmail(null);
        customer.setPhone(null);
        customer.setBirthDate(null);
        customer.setAddress(null);
        customer.setExternalAuthUid(null);
        customer.setMarketingConsent(false);
        customer.setActive(false);
        customer.setDeletedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        customerAuthRepository.findByCustomer_Id(id).ifPresent(auth -> {
            auth.setLoginEmail("anonymous+" + marker + "@mottainai.invalid");
            auth.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            auth.setRecoveryTokenHash(null);
            auth.setRecoveryExpiresAt(null);
            auth.setActive(false);
            auth.setUpdatedAt(LocalDateTime.now());
            customerAuthRepository.save(auth);
        });
        loyaltyAccountRepository.findByCustomer_Id(id).ifPresent(account -> {
            account.setActive(false);
            loyaltyAccountRepository.save(account);
        });
        geofenceRepository.findByCustomer_IdAndActiveTrueOrderByCreatedAtDesc(id)
            .forEach(geofence -> geofence.setActive(false));
        customerRepository.save(customer);
        auditLogRepository.record("customer", "UPDATE", id.toString(), null, null, Map.of("anonymized", true));
    }

    @Transactional(readOnly = true)
    public List<CustomerGeofenceResponse> geofences(Integer customerId) {
        customer(customerId);
        return geofenceRepository.findByCustomer_IdAndActiveTrueOrderByCreatedAtDesc(customerId)
            .stream()
            .map(CustomerGeofenceResponse::fromEntity)
            .toList();
    }

    @Transactional
    public CustomerGeofenceResponse addGeofence(Integer customerId, CreateCustomerGeofenceRequest request) {
        Customer customer = customer(customerId);
        RetailStore store = retailStoreRepository.findByIdAndActiveTrueAndDeletedAtIsNull(request.storeId())
            .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        CustomerGeofence geofence = geofenceRepository.findByCustomer_IdAndStore_Id(customerId, request.storeId())
            .orElseGet(CustomerGeofence::new);
        LocalDateTime now = LocalDateTime.now();
        if (geofence.getId() == null) {
            geofence.setCustomer(customer);
            geofence.setStore(store);
            geofence.setCreatedAt(now);
        }

        geofence.setRadiusMeters(request.radiusMeters());
        geofence.setActive(true);
        geofence.setUpdatedAt(now);
        return CustomerGeofenceResponse.fromEntity(geofenceRepository.save(geofence));
    }

    @Transactional
    public void removeGeofence(Integer customerId, Integer geofenceId) {
        CustomerGeofence geofence = geofenceRepository.findByIdAndCustomer_Id(geofenceId, customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Geofence not found"));
        geofence.setActive(false);
        geofence.setUpdatedAt(LocalDateTime.now());
        geofenceRepository.save(geofence);
    }

    @Transactional
    public LoyaltyAccountResponse updateLoyaltyStatus(Integer customerId, UpdateLoyaltyStatusRequest request) {
        customer(customerId);
        LoyaltyAccount account = loyaltyAccountRepository.findByCustomer_Id(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Loyalty account not found"));
        account.setActive(request.active());
        account.setUpdatedAt(LocalDateTime.now());
        loyaltyAccountRepository.save(account);
        auditLogRepository.record("loyalty_account", "UPDATE", account.getId().toString(), null, null,
                Map.of("active", request.active()));
        return LoyaltyAccountResponse.fromEntity(account);
    }

    private Customer customer(Integer id) {
        return customerRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    private Address toAddress(CreateAddressRequest request) {
        Address address = new Address();
        address.setZipCode(request.zipCode().trim());
        address.setStreet(request.street().trim());
        address.setNumber(request.number().trim());
        address.setComplement(normalize(request.complement()));
        address.setNeighborhood(request.neighborhood().trim());
        address.setCity(request.city().trim());
        address.setState(request.state().trim());
        return address;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void validatePassword(String password) {
        boolean upper = password.chars().anyMatch(Character::isUpperCase);
        boolean lower = password.chars().anyMatch(Character::isLowerCase);
        boolean number = password.chars().anyMatch(Character::isDigit);
        boolean special = password.chars().anyMatch(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c));
        if (!upper || !lower || !number || !special) {
            throw new BusinessException("Password must contain uppercase, lowercase, number and special character");
        }
    }

}
