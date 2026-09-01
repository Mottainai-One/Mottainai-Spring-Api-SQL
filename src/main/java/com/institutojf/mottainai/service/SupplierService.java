package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.CreateSupplierRequest;
import com.institutojf.mottainai.dto.request.UpdateSupplierRequest;
import com.institutojf.mottainai.dto.response.SupplierResponse;
import com.institutojf.mottainai.exception.BusinessException;
import com.institutojf.mottainai.exception.ConflictException;
import com.institutojf.mottainai.exception.ResourceNotFoundException;
import com.institutojf.mottainai.mapper.SupplierMapper;
import com.institutojf.mottainai.model.Address;
import com.institutojf.mottainai.model.Supplier;
import com.institutojf.mottainai.repository.AddressRepository;
import com.institutojf.mottainai.repository.SupplierProductRepository;
import com.institutojf.mottainai.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final AddressRepository addressRepository;
    private final SupplierProductRepository supplierProductRepository;
    private final SupplierMapper supplierMapper;

    public SupplierService(SupplierRepository supplierRepository, AddressRepository addressRepository, SupplierProductRepository supplierProductRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.addressRepository = addressRepository;
        this.supplierProductRepository = supplierProductRepository;
        this.supplierMapper = supplierMapper;
    }

    @Transactional
    public SupplierResponse create(CreateSupplierRequest request) {
        if (supplierRepository.existsByCnpj(request.cnpj())) {
            throw new ConflictException("Supplier CNPJ already exists");
        }

        Supplier supplier = new Supplier();
        supplier.setCnpj(request.cnpj());
        supplier.setActive(true);
        applySupplierFields(
                request.addressId(), request.tradeName(), request.email(), request.phone(), supplier
        );

        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Transactional(readOnly = true)
    public Page<SupplierResponse> findAll(Pageable pageable) {
        return supplierRepository.findAllByActiveTrueAndDeletedAtIsNull(pageable).map(supplierMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public SupplierResponse findById(Integer id) {
        return supplierMapper.toResponse(findActiveSupplierById(id));
    }

    @Transactional
    public SupplierResponse update(Integer id, UpdateSupplierRequest request) {
        Supplier supplier = findSupplierById(id);
        if (Boolean.FALSE.equals(request.active())) {
            ensureCanDeactivate(id);
        }
        applySupplierFields(
                request.addressId(), request.tradeName(), request.email(), request.phone(), supplier
        );
        supplier.setActive(request.active());

        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public void deactivate(Integer id) {
        Supplier supplier = findActiveSupplierById(id);
        ensureCanDeactivate(id);
        supplier.setActive(false);
        supplierRepository.save(supplier);
    }

    private void ensureCanDeactivate(Integer id) {
        if (supplierProductRepository.existsBySupplier_IdAndActiveTrueAndDeletedAtIsNull(id)) {
            throw new BusinessException("Supplier has active product links");
        }
    }

    private Supplier findActiveSupplierById(Integer id) {
        return supplierRepository.findByIdAndActiveTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
    }

    private void applySupplierFields(Integer addressId, String tradeName, String email, String phone, Supplier supplier) {
        Address address = addressRepository.findByIdAndDeletedAtIsNull(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        supplier.setAddress(address);
        supplier.setTradeName(tradeName);
        supplier.setEmail(email);
        supplier.setPhone(phone);
    }

    private Supplier findSupplierById(Integer id) {
        return supplierRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
    }
}
