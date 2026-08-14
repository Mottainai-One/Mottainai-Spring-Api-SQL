package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.CreateSupplierRequest;
import com.institutojf.mottainai.dto.request.UpdateSupplierRequest;
import com.institutojf.mottainai.exception.BusinessException;
import com.institutojf.mottainai.exception.ConflictException;
import com.institutojf.mottainai.exception.ResourceNotFoundException;
import com.institutojf.mottainai.mapper.SupplierMapper;
import com.institutojf.mottainai.model.Address;
import com.institutojf.mottainai.model.Supplier;
import com.institutojf.mottainai.repository.AddressRepository;
import com.institutojf.mottainai.repository.SupplierRepository;
import com.institutojf.mottainai.repository.SupplierProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private SupplierProductRepository supplierProductRepository;

    @Mock
    private SupplierMapper supplierMapper;

    @InjectMocks
    private SupplierService supplierService;

    @Test
    @DisplayName("Should reject supplier when CNPJ already exists")
    void shouldRejectSupplierWhenCnpjAlreadyExists() {
        CreateSupplierRequest request = request();
        when(supplierRepository.existsByCnpj(request.cnpj())).thenReturn(true);

        assertThrows(ConflictException.class, () -> supplierService.create(request));

        verify(addressRepository, never()).findByIdAndDeletedAtIsNull(any());
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject supplier when address does not exist")
    void shouldRejectSupplierWhenAddressDoesNotExist() {
        CreateSupplierRequest request = request();
        when(supplierRepository.existsByCnpj(request.cnpj())).thenReturn(false);
        when(addressRepository.findByIdAndDeletedAtIsNull(request.addressId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> supplierService.create(request));

        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should deactivate supplier without soft deleting it")
    void shouldDeactivateSupplierWithoutSoftDeletingIt() {
        Supplier supplier = supplier(1, true);
        when(supplierRepository.findByIdAndActiveTrueAndDeletedAtIsNull(1)).thenReturn(Optional.of(supplier));

        supplierService.deactivate(1);

        assertFalse(supplier.getActive());
        assertNull(supplier.getDeletedAt());
        verify(supplierRepository).save(supplier);
    }

    @Test
    @DisplayName("Should reject supplier deactivation when it has active product links")
    void shouldRejectSupplierDeactivationWhenItHasActiveProductLinks() {
        Supplier supplier = supplier(1, true);
        when(supplierRepository.findByIdAndActiveTrueAndDeletedAtIsNull(1)).thenReturn(Optional.of(supplier));
        when(supplierProductRepository.existsBySupplier_IdAndActiveTrueAndDeletedAtIsNull(1)).thenReturn(true);

        assertThrows(BusinessException.class, () -> supplierService.deactivate(1));

        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return not found when deactivating a nonexistent supplier")
    void shouldReturnNotFoundWhenDeactivatingANonexistentSupplier() {
        when(supplierRepository.findByIdAndActiveTrueAndDeletedAtIsNull(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> supplierService.deactivate(1));

        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update supplier fields and reactivate it")
    void shouldUpdateSupplierFieldsAndReactivateIt() {
        Supplier supplier = supplier(1, false);
        Address address = new Address();
        address.setId(2);
        when(supplierRepository.findByIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(supplier));
        when(addressRepository.findByIdAndDeletedAtIsNull(2)).thenReturn(Optional.of(address));
        when(supplierRepository.save(supplier)).thenReturn(supplier);

        supplierService.update(1, new UpdateSupplierRequest(
                2, "Fornecedor Atualizado", "novo@fornecedor.com", "11888888888", true
        ));

        assertEquals(address, supplier.getAddress());
        assertEquals("Fornecedor Atualizado", supplier.getTradeName());
        assertEquals("novo@fornecedor.com", supplier.getEmail());
        assertEquals("11888888888", supplier.getPhone());
        assertTrue(supplier.getActive());
        verify(supplierRepository).save(supplier);
    }

    private Supplier supplier(Integer id, boolean active) {
        Supplier supplier = new Supplier();
        supplier.setId(id);
        supplier.setActive(active);
        return supplier;
    }

    private CreateSupplierRequest request() {
        return new CreateSupplierRequest(
                1, "Fornecedor Teste", "11222333000181", "fornecedor@teste.com", "11999999999"
        );
    }
}
