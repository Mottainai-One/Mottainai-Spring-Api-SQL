package com.institutojf.mottainai.repository;

import com.institutojf.mottainai.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    /**
     * Busca um produto disponível para uso operacional pelo identificador.
     * Produtos inativos ou excluídos logicamente não são retornados.
     */
    Optional<Product> findByIdAndActiveTrueAndDeletedAtIsNull(Integer id);

    Optional<Product> findByIdAndDeletedAtIsNull(Integer id);

    /**
     * Busca um produto disponível para uso operacional pelo código de barras.
     * Este método será usado em fluxos, como consulta de SKU e recebimento de mercadoria.
     */
    Optional<Product> findByBarcodeAndActiveTrueAndDeletedAtIsNull(String barcode);

    /**
     * Lista apenas produtos disponíveis para uso operacional, de forma paginada.
     */
    Page<Product> findAllByActiveTrueAndDeletedAtIsNull(Pageable pageable);

    /**
     * Verifica se o código de barras já foi cadastrado.
     * A consulta considera inclusive produtos inativos, pois o banco exige barcode único globalmente.
     */
    boolean existsByBarcode(String barcode);

    boolean existsByCategory_IdAndActiveTrueAndDeletedAtIsNull(Integer categoryId);
}
