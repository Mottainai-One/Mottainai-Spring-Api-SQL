package com.institutojf.mottainai.service;

import com.institutojf.mottainai.model.AppUser;
import com.institutojf.mottainai.repository.AppUserRepository;
import com.institutojf.mottainai.repository.StaffSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RlsContextService {
    private final JdbcTemplate jdbcTemplate;
    private final AppUserRepository appUserRepository;
    private final StaffSessionRepository staffSessionRepository;

    // Inicializa o contexto da transação para o login por CPF
    public boolean bootstrapByCpf(String cpf) {
        return bootstrap(cpf, "CPF");
    }

    // Permite consultar um convite pendente antes da ativação da conta
    public boolean bootstrapInvitedByCpf(String cpf) {
        return bootstrap(cpf, "INVITED_CPF");
    }

    // Reconstitui o contexto RLS do funcionário pelo email do JWT
    public boolean bootstrapByEmail(String email) {
        return bootstrap(email, "EMAIL");
    }

    // Localiza o dono de um token válido e define o contexto RLS
    public Integer bootstrapByToken(String tokenHash, boolean invitation) {
        return jdbcTemplate.queryForObject(
                "SELECT mottainai.fn_bootstrap_staff_context_by_token(?, ?)",
                Integer.class, tokenHash, invitation);
    }

    /**
     * Compara a versão do JWT com a versão atual do usuário e verifica a sessão
     * Quando a senha muda ou a sessão é revogada, tokens antigos deixam de funcionar
     */
    @Transactional(readOnly = true)
    public boolean validateAccessToken(Jwt jwt) {
        if (!"access".equals(jwt.getClaimAsString("use")) || !bootstrapByEmail(jwt.getSubject())) {
            return false;
        }

        UUID sessionId;
        try {
            sessionId = UUID.fromString(jwt.getClaimAsString("sid"));
        } catch (RuntimeException exception) {
            return false;
        }

        return appUserRepository.findByEmailIgnoreCaseAndActiveTrueAndDeletedAtIsNull(jwt.getSubject())
                .filter(user -> isCurrentUser(user, jwt, sessionId))
                .isPresent();
    }

    // A função restrita do banco valida a identidade e define empresa e loja
    private boolean bootstrap(String identity, String lookup) {
        Boolean initialized = jdbcTemplate.queryForObject(
                "SELECT mottainai.fn_bootstrap_staff_context(?, ?)",
                Boolean.class, identity, lookup);
        return Boolean.TRUE.equals(initialized);
    }

    private boolean isCurrentUser(AppUser user, Jwt jwt, UUID sessionId) {
        if (user.getEmployee() == null || user.getEmployee().getRole() == null
                || !Boolean.TRUE.equals(user.getEmployee().getActive())
                || user.getEmployee().getDeletedAt() != null
                || !Boolean.TRUE.equals(user.getEmployee().getRole().getActive())
                || user.getEmployee().getRole().getDeletedAt() != null) {
            return false;
        }

        Object version = jwt.getClaim("tokenVersion");
        List<String> roles = jwt.getClaimAsStringList("roles");
        String currentRole = user.getEmployee().getRole().getName().toUpperCase(Locale.ROOT);
        return version instanceof Number tokenVersion
                && user.getTokenVersion().equals(tokenVersion.intValue())
                && roles != null && roles.size() == 1 && roles.contains(currentRole)
                && staffSessionRepository.isActive(sessionId, user.getId());
    }
}
