package com.institutojf.mottainai.repository;

import com.institutojf.mottainai.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {

    // Busca o último código que ainda não foi usado
    Optional<PasswordResetToken> findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(Integer userId);

    // Remove códigos anteriores quando um novo é solicitado
    void deleteByUserId(Integer userId);
}
