package com.institutojf.mottainai.repository;

import com.institutojf.mottainai.model.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Integer> {
    List<LoyaltyTransaction> findByLoyaltyAccount_IdOrderByCreatedAtDesc(Integer loyaltyAccountId);
}
