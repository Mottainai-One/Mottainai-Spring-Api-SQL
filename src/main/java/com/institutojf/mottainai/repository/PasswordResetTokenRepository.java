package com.institutojf.mottainai.repository;

import com.institutojf.mottainai.model.PasswordResetToken;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {

    // Busca o último código que ainda não foi usado
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM PasswordResetToken t WHERE t.user.id = :userId AND t.usedAt IS NULL ORDER BY t.createdAt DESC")
    Optional<PasswordResetToken> findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(@Param("userId") Integer userId);

    // Remove códigos anteriores quando um novo é solicitado
    void deleteByUserId(Integer userId);
}
