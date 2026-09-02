package com.institutojf.mottainai.repository;

import com.institutojf.mottainai.model.TaxProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaxProfileRepository extends JpaRepository<TaxProfile, Integer> {
    Optional<TaxProfile> findByIdAndActiveTrueAndDeletedAtIsNull(Integer id);
}
