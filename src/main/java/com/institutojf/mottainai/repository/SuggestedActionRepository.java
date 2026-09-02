package com.institutojf.mottainai.repository;

import com.institutojf.mottainai.model.SuggestedAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuggestedActionRepository extends JpaRepository<SuggestedAction, Integer> {
    List<SuggestedAction> findByAlert_Store_StoreIdOrderByGeneratedAtDesc(Integer storeId);
}
