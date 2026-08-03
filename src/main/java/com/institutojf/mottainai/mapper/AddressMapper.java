package com.institutojf.mottainai.mapper;

import com.institutojf.mottainai.dto.response.AddressResponse;
import com.institutojf.mottainai.model.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public AddressResponse toResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getZipCode(),
                address.getStreet(),
                address.getNumber(),
                address.getComplement(),
                address.getNeighborhood(),
                address.getCity(),
                address.getState()
        );
    }
}
