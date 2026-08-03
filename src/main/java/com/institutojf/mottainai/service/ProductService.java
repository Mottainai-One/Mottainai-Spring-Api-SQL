package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.CreateProductRequest;
import com.institutojf.mottainai.dto.request.UpdateProductRequest;
import com.institutojf.mottainai.dto.response.ProductResponse;
import com.institutojf.mottainai.exception.BusinessException;
import com.institutojf.mottainai.exception.ConflictException;
import com.institutojf.mottainai.exception.ResourceNotFoundException;
import com.institutojf.mottainai.mapper.ProductMapper;
import com.institutojf.mottainai.model.Product;
import com.institutojf.mottainai.model.ProductCategory;
import com.institutojf.mottainai.repository.ProductCategoryRepository;
import com.institutojf.mottainai.repository.ProductRepository;
import com.institutojf.mottainai.repository.SupplierProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final SupplierProductRepository supplierProductRepository;
    private final ProductMapper productMapper;

    public ProductService(
            ProductRepository productRepository,
            ProductCategoryRepository categoryRepository,
            SupplierProductRepository supplierProductRepository,
            ProductMapper productMapper
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierProductRepository = supplierProductRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        if (productRepository.existsByBarcode(request.barcode())) {
            throw new ConflictException("Product barcode already exists");
        }

        Product product = new Product();
        product.setBarcode(request.barcode());
        product.setActive(true);
        applyProductFields(
                request.categoryId(), request.name(), request.description(), request.brand(),
                request.unitMeasure(), request.weight(), product
        );

        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> findAll(Pageable pageable) {
        return productRepository.findAllByActiveTrueAndDeletedAtIsNull(pageable).map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Integer id) {
        return productMapper.toResponse(findActiveProductById(id));
    }

    @Transactional(readOnly = true)
    public ProductResponse findByBarcode(String barcode) {
        Product product = productRepository.findByBarcodeAndActiveTrueAndDeletedAtIsNull(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return productMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse update(Integer id, UpdateProductRequest request) {
        Product product = findProductById(id);
        if (Boolean.FALSE.equals(request.active())) {
            ensureCanDeactivate(id);
        }
        applyProductFields(
                request.categoryId(), request.name(), request.description(), request.brand(),
                request.unitMeasure(), request.weight(), product
        );
        product.setActive(request.active());

        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public void deactivate(Integer id) {
        Product product = findActiveProductById(id);
        ensureCanDeactivate(id);
        product.setActive(false);
        productRepository.save(product);
    }

    private void ensureCanDeactivate(Integer id) {
        if (supplierProductRepository.existsByProduct_IdAndActiveTrueAndDeletedAtIsNull(id)) {
            throw new BusinessException("Product has active supplier links");
        }
    }

    private Product findActiveProductById(Integer id) {
        return productRepository.findByIdAndActiveTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private void applyProductFields(Integer categoryId, String name, String description, String brand, String unitMeasure, BigDecimal weight, Product product) {
        ProductCategory category = categoryRepository.findByIdAndActiveTrueAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Product category not found"));

        product.setCategory(category);
        product.setName(name);
        product.setDescription(description);
        product.setBrand(brand);
        product.setUnitMeasure(unitMeasure);
        product.setWeight(weight);
    }

    private Product findProductById(Integer id) {
        return productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }
}
