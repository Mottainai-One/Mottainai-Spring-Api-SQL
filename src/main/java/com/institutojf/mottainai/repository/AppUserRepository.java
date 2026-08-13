package com.institutojf.mottainai.repository;

import com.institutojf.mottainai.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Integer> {

    /**
     * Busca usuários que podem fazer login, ou seja, que estão ativos e não foram deletados
     */
    Optional<AppUser> findByEmailIgnoreCaseAndActiveTrueAndDeletedAtIsNull(String email);
}
