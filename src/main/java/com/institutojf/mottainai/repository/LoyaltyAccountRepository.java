package com.institutojf.mottainai.repository;

import com.institutojf.mottainai.model.LoyaltyAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, Integer> {

    Optional<LoyaltyAccount> findByCustomer_Id(Integer customerId);

}
