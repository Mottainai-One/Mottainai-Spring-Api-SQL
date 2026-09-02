package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.CreateRetailStoreRequest;
import com.institutojf.mottainai.dto.request.UpdateRetailStoreRequest;
import com.institutojf.mottainai.dto.response.RetailStoreResponse;
import com.institutojf.mottainai.exception.BusinessException;
import com.institutojf.mottainai.exception.ConflictException;
import com.institutojf.mottainai.exception.ResourceNotFoundException;
import com.institutojf.mottainai.mapper.RetailStoreMapper;
import com.institutojf.mottainai.model.Address;
import com.institutojf.mottainai.model.Company;
import com.institutojf.mottainai.model.RetailStore;
import com.institutojf.mottainai.repository.AddressRepository;
import com.institutojf.mottainai.repository.CompanyRepository;
import com.institutojf.mottainai.repository.RetailStoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RetailStoreService {

    private final RetailStoreRepository retailStoreRepository;
    private final CompanyRepository companyRepository;
    private final AddressRepository addressRepository;
    private final RetailStoreMapper retailStoreMapper;

    public RetailStoreService(RetailStoreRepository retailStoreRepository, CompanyRepository companyRepository, AddressRepository addressRepository, RetailStoreMapper retailStoreMapper) {
        this.retailStoreRepository = retailStoreRepository;
        this.companyRepository = companyRepository;
        this.addressRepository = addressRepository;
        this.retailStoreMapper = retailStoreMapper;
    }

    @Transactional
    public RetailStoreResponse create(CreateRetailStoreRequest request) {
        if (retailStoreRepository.existsByCnpj(request.cnpj())) {
            throw new ConflictException("Store CNPJ already exists");
        }

        Company company = findActiveCompanyById(request.companyId());
        ensureStoreLimitNotReached(company);

        RetailStore store = new RetailStore();
        store.setCompany(company);
        store.setCnpj(request.cnpj());
        store.setActive(true);
        applyStoreFields(request.addressId(), request.name(), request.email(), request.phone(), request.latitude(), request.longitude(), store);

        return retailStoreMapper.toResponse(retailStoreRepository.save(store));
    }

    @Transactional(readOnly = true)
    public Page<RetailStoreResponse> findAll(Pageable pageable) {
        return retailStoreRepository.findAllByActiveTrueAndDeletedAtIsNull(pageable).map(retailStoreMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public RetailStoreResponse findById(Integer id) {
        return retailStoreMapper.toResponse(findActiveStoreById(id));
    }

    @Transactional
    public RetailStoreResponse update(Integer id, UpdateRetailStoreRequest request) {
        RetailStore store = findStoreById(id);
        // Reativar uma loja também consome uma vaga do plano contratado
        if (Boolean.TRUE.equals(request.active()) && Boolean.FALSE.equals(store.getActive())) {
            ensureStoreLimitNotReached(store.getCompany());
        }
        applyStoreFields(request.addressId(), request.name(), request.email(), request.phone(), request.latitude(), request.longitude(), store);
        store.setActive(request.active());

        return retailStoreMapper.toResponse(retailStoreRepository.save(store));
    }

    @Transactional
    public void deactivate(Integer id) {
        RetailStore store = findActiveStoreById(id);
        store.setActive(false);
        retailStoreRepository.save(store);
    }

    /**
     * Garante que a empresa não ultrapasse o número de lojas contratado no plano de assinatura
     */
    private void ensureStoreLimitNotReached(Company company) {
        long activeStores = retailStoreRepository.countByCompany_IdAndActiveTrueAndDeletedAtIsNull(company.getId());
        if (activeStores >= company.getPlan().getStoreLimit()) {
            throw new BusinessException("Company reached the store limit of its subscription plan");
        }
    }

    private Company findActiveCompanyById(Integer id) {
        return companyRepository.findByIdAndActiveTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
    }

    private RetailStore findActiveStoreById(Integer id) {
        return retailStoreRepository.findByIdAndActiveTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));
    }

    private RetailStore findStoreById(Integer id) {
        return retailStoreRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));
    }

    private void applyStoreFields(Integer addressId, String name, String email, String phone, java.math.BigDecimal latitude, java.math.BigDecimal longitude, RetailStore store) {
        Address address = addressRepository.findByIdAndDeletedAtIsNull(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        store.setAddress(address);
        store.setName(name);
        store.setEmail(email);
        store.setPhone(phone);
        store.setLatitude(latitude);
        store.setLongitude(longitude);
    }
}
