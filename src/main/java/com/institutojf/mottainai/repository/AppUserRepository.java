package com.institutojf.mottainai.repository;

import com.institutojf.mottainai.model.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Integer> {

    /**
     * Busca usuários que podem fazer login, ou seja, que estão ativos e não foram deletados
     */
    Optional<AppUser> findByEmailIgnoreCaseAndActiveTrueAndDeletedAtIsNull(String email);

    boolean existsByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    @EntityGraph(attributePaths = {"employee", "employee.store", "employee.role"})
    Optional<AppUser> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    @EntityGraph(attributePaths = {"employee", "employee.store", "employee.role"})
    List<AppUser> findAllByDeletedAtIsNull();

}
