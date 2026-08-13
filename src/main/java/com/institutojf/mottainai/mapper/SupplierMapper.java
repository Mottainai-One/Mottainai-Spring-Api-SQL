package com.institutojf.mottainai.mapper;

import com.institutojf.mottainai.dto.response.SupplierResponse;
import com.institutojf.mottainai.model.Supplier;
import org.springframework.stereotype.Component;

/**
 * Transforma um fornecedor e seu endereço no formato de resposta da API
 */
@Component
public class SupplierMapper {

    private final AddressMapper addressMapper;

    public SupplierMapper(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                addressMapper.toResponse(supplier.getAddress()),
                supplier.getTradeName(),
                supplier.getCnpj(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getActive()
        );
    }
}
