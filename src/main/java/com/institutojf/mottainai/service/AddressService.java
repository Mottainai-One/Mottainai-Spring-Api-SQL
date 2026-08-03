package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.CreateAddressRequest;
import com.institutojf.mottainai.dto.request.UpdateAddressRequest;
import com.institutojf.mottainai.dto.response.AddressResponse;
import com.institutojf.mottainai.exception.ConflictException;
import com.institutojf.mottainai.exception.ResourceNotFoundException;
import com.institutojf.mottainai.mapper.AddressMapper;
import com.institutojf.mottainai.model.Address;
import com.institutojf.mottainai.repository.AddressRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    public AddressService(AddressRepository addressRepository, AddressMapper addressMapper) {
        this.addressRepository = addressRepository;
        this.addressMapper = addressMapper;
    }

    @Transactional
    public AddressResponse create(CreateAddressRequest request) {
        validateAddressUniqueness(
                request.zipCode(), request.street(), request.number(), request.complement(), null
        );

        Address address = new Address();
        applyCreateRequest(request, address);
        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Transactional(readOnly = true)
    public Page<AddressResponse> findAll(Pageable pageable) {
        return addressRepository.findAllByDeletedAtIsNull(pageable).map(addressMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AddressResponse findById(Integer id) {
        return addressMapper.toResponse(findAddressById(id));
    }

    @Transactional
    public AddressResponse update(Integer id, UpdateAddressRequest request) {
        Address address = findAddressById(id);
        validateAddressUniqueness(
                request.zipCode(), request.street(), request.number(), request.complement(), id
        );
        applyUpdateRequest(request, address);
        return addressMapper.toResponse(addressRepository.save(address));
    }

    private Address findAddressById(Integer id) {
        return addressRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    }

    private void validateAddressUniqueness(
            String zipCode,
            String street,
            String number,
            String complement,
            Integer excludedId
    ) {
        if (addressRepository.existsActiveAddress(
                zipCode.trim(), street.trim(), number.trim(), normalizeOptional(complement), excludedId
        )) {
            throw new ConflictException("Address already exists");
        }
    }

    private void applyCreateRequest(CreateAddressRequest request, Address address) {
        applyAddressFields(
                request.zipCode(), request.street(), request.number(), request.complement(),
                request.neighborhood(), request.city(), request.state(), address
        );
    }

    private void applyUpdateRequest(UpdateAddressRequest request, Address address) {
        applyAddressFields(
                request.zipCode(), request.street(), request.number(), request.complement(),
                request.neighborhood(), request.city(), request.state(), address
        );
    }

    private void applyAddressFields(
            String zipCode,
            String street,
            String number,
            String complement,
            String neighborhood,
            String city,
            String state,
            Address address
    ) {
        address.setZipCode(zipCode.trim());
        address.setStreet(street.trim());
        address.setNumber(number.trim());
        address.setComplement(normalizeOptional(complement));
        address.setNeighborhood(neighborhood.trim());
        address.setCity(city.trim());
        address.setState(state.trim());
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String normalizedValue = value.trim();
        return normalizedValue.isEmpty() ? null : normalizedValue;
    }
}
