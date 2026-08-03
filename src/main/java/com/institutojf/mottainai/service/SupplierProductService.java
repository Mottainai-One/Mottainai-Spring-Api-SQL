package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.CreateSupplierProductRequest;
import com.institutojf.mottainai.dto.request.UpdateSupplierProductRequest;
import com.institutojf.mottainai.dto.response.SupplierProductResponse;
import com.institutojf.mottainai.exception.ConflictException;
import com.institutojf.mottainai.exception.ResourceNotFoundException;
import com.institutojf.mottainai.mapper.SupplierProductMapper;
import com.institutojf.mottainai.model.Product;
import com.institutojf.mottainai.model.Supplier;
import com.institutojf.mottainai.model.SupplierProduct;
import com.institutojf.mottainai.repository.ProductRepository;
import com.institutojf.mottainai.repository.SupplierProductRepository;
import com.institutojf.mottainai.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierProductService {

    private final SupplierProductRepository supplierProductRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final SupplierProductMapper supplierProductMapper;

    public SupplierProductService(SupplierProductRepository supplierProductRepository, SupplierRepository supplierRepository, ProductRepository productRepository, SupplierProductMapper supplierProductMapper) {
        this.supplierProductRepository = supplierProductRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.supplierProductMapper = supplierProductMapper;
    }

    @Transactional
    public SupplierProductResponse create(CreateSupplierProductRequest request) {
        if (supplierProductRepository.existsBySupplier_IdAndProduct_Id(request.supplierId(), request.productId())) {
            throw new ConflictException("Supplier is already linked to this product");
        }

        Supplier supplier = supplierRepository.findByIdAndActiveTrueAndDeletedAtIsNull(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        Product product = productRepository.findByIdAndActiveTrueAndDeletedAtIsNull(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        SupplierProduct supplierProduct = new SupplierProduct();
        supplierProduct.setSupplier(supplier);
        supplierProduct.setProduct(product);
        supplierProduct.setSupplierCode(request.supplierCode());
        supplierProduct.setPurchasePrice(request.purchasePrice());
        supplierProduct.setLeadTime(request.leadTime());
        supplierProduct.setActive(true);

        return supplierProductMapper.toResponse(supplierProductRepository.save(supplierProduct));
    }

    @Transactional(readOnly = true)
    public Page<SupplierProductResponse> findAll(Pageable pageable) {
        return supplierProductRepository.findAllByActiveTrueAndDeletedAtIsNull(pageable)
                .map(supplierProductMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public SupplierProductResponse findById(Integer id) {
        return supplierProductMapper.toResponse(findActiveSupplierProductById(id));
    }

    @Transactional
    public SupplierProductResponse update(Integer id, UpdateSupplierProductRequest request) {
        SupplierProduct supplierProduct = findSupplierProductById(id);
        supplierProduct.setSupplierCode(request.supplierCode());
        supplierProduct.setPurchasePrice(request.purchasePrice());
        supplierProduct.setLeadTime(request.leadTime());
        supplierProduct.setActive(request.active());


        return supplierProductMapper.toResponse(supplierProductRepository.save(supplierProduct));
    }

    @Transactional
    public void deactivate(Integer id) {
        SupplierProduct supplierProduct = findActiveSupplierProductById(id);
        supplierProduct.setActive(false);
        supplierProductRepository.save(supplierProduct);
    }

    private SupplierProduct findActiveSupplierProductById(Integer id) {
        return supplierProductRepository.findByIdAndActiveTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier product link not found"));
    }

    private SupplierProduct findSupplierProductById(Integer id) {
        return supplierProductRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier product link not found"));
    }
}
