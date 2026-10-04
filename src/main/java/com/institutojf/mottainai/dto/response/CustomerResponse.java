package com.institutojf.mottainai.dto.response;

import com.institutojf.mottainai.model.Customer;

import java.time.*;

public record CustomerResponse(
        Integer id,
        String fullName,
        String cpf,
        String email,
        String phone,
        LocalDate birthDate,
        Boolean active,
        Boolean marketingConsent,
        AddressResponse address,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CustomerResponse fromEntity(Customer customer) {
        var address = customer.getAddress();
        AddressResponse addressResponse = address == null ? null
                : new AddressResponse(address.getId(), address.getZipCode(), address.getStreet(), address.getNumber(),
                        address.getComplement(), address.getNeighborhood(), address.getCity(), address.getState());
        return new CustomerResponse(customer.getId(), customer.getFullName(), maskCpf(customer.getCpf()),
                customer.getEmail(), customer.getPhone(), customer.getBirthDate(), customer.getActive(),
                customer.getMarketingConsent(), addressResponse, customer.getCreatedAt(), customer.getUpdatedAt());
    }

    private static String maskCpf(String cpf) {
        return cpf == null || cpf.length() != 11 ? null
                : "***." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-**";
    }
}
