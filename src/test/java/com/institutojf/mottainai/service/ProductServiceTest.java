package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.CreateProductRequest;
import com.institutojf.mottainai.dto.request.UpdateProductRequest;
import com.institutojf.mottainai.exception.BusinessException;
import com.institutojf.mottainai.exception.ConflictException;
import com.institutojf.mottainai.exception.ResourceNotFoundException;
import com.institutojf.mottainai.mapper.ProductMapper;
import com.institutojf.mottainai.model.Product;
import com.institutojf.mottainai.model.ProductCategory;
import com.institutojf.mottainai.repository.ProductCategoryRepository;
import com.institutojf.mottainai.repository.ProductRepository;
import com.institutojf.mottainai.repository.SupplierProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductCategoryRepository categoryRepository;

    @Mock
    private SupplierProductRepository supplierProductRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("Should deactivate product without soft deleting it")
    void shouldDeactivateProductWithoutSoftDeletingIt() {
        Product product = product(1, true);
        when(productRepository.findByIdAndActiveTrueAndDeletedAtIsNull(1)).thenReturn(Optional.of(product));

        productService.deactivate(1);

        assertFalse(product.getActive());
        assertNull(product.getDeletedAt());
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should reject product deactivation when it has active supplier links")
    void shouldRejectProductDeactivationWhenItHasActiveSupplierLinks() {
        Product product = product(1, true);
        when(productRepository.findByIdAndActiveTrueAndDeletedAtIsNull(1)).thenReturn(Optional.of(product));
        when(supplierProductRepository.existsByProduct_IdAndActiveTrueAndDeletedAtIsNull(1)).thenReturn(true);

        assertThrows(BusinessException.class, () -> productService.deactivate(1));

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return not found when deactivating a nonexistent product")
    void shouldReturnNotFoundWhenDeactivatingANonexistentProduct() {
        when(productRepository.findByIdAndActiveTrueAndDeletedAtIsNull(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.deactivate(1));

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update product fields and reactivate it")
    void shouldUpdateProductFieldsAndReactivateIt() {
        Product product = product(1, false);
        ProductCategory category = category();
        when(productRepository.findByIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(product));
        when(categoryRepository.findByIdAndActiveTrueAndDeletedAtIsNull(1)).thenReturn(Optional.of(category));
        when(productRepository.save(product)).thenReturn(product);

        productService.update(1, new UpdateProductRequest(
                1, "Brown rice", "Whole grain", "Mottainai", "KG", new BigDecimal("1.25"), true
        ));

        assertEquals(category, product.getCategory());
        assertEquals("Brown rice", product.getName());
        assertEquals("Whole grain", product.getDescription());
        assertEquals("Mottainai", product.getBrand());
        assertEquals("KG", product.getUnitMeasure());
        assertEquals(new BigDecimal("1.25"), product.getWeight());
        assertTrue(product.getActive());
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should reject product when barcode already exists")
    void shouldRejectProductWhenBarcodeAlreadyExists() {
        CreateProductRequest request = createRequest();
        when(productRepository.existsByBarcode(request.barcode())).thenReturn(true);

        assertThrows(ConflictException.class, () -> productService.create(request));

        verify(categoryRepository, never()).findByIdAndActiveTrueAndDeletedAtIsNull(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject product when category does not exist")
    void shouldRejectProductWhenCategoryDoesNotExist() {
        CreateProductRequest request = createRequest();
        when(productRepository.existsByBarcode(request.barcode())).thenReturn(false);
        when(categoryRepository.findByIdAndActiveTrueAndDeletedAtIsNull(request.categoryId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.create(request));

        verify(productRepository, never()).save(any());
    }

    private CreateProductRequest createRequest() {
        return new CreateProductRequest(1, "123", "Rice", null, null, "KG", BigDecimal.ONE);
    }

    private Product product(Integer id, boolean active) {
        Product product = new Product();
        product.setId(id);
        product.setCategory(category());
        product.setBarcode("123");
        product.setName("Rice");
        product.setUnitMeasure("KG");
        product.setActive(active);
        product.setVersion(1);
        return product;
    }

    private ProductCategory category() {
        ProductCategory category = new ProductCategory();
        category.setId(1);
        category.setName("Food");
        category.setActive(true);
        return category;
    }
}
