package com.institutojf.mottainai.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EmployeeInvitationTokenRepository {
    private final JdbcTemplate jdbcTemplate;

    public void create(UUID invitationId, Integer userId, String tokenHash, OffsetDateTime expiresAt) {
        jdbcTemplate.update("""
                INSERT INTO mottainai.employee_invitation_token
                    (invitation_id, user_id, token_hash, expires_at)
                VALUES (?, ?, ?, ?)
                """, invitationId, userId, tokenHash, expiresAt);
    }

    public Optional<Invitation> findUnusedByHashForUpdate(String tokenHash) {
        List<Invitation> invitations = jdbcTemplate.query("""
                SELECT invitation_id, user_id, expires_at
                  FROM mottainai.employee_invitation_token
                 WHERE token_hash = ? AND used_at IS NULL
                 FOR UPDATE
                """, (resultSet, rowNumber) -> new Invitation(
                resultSet.getObject("invitation_id", UUID.class),
                resultSet.getInt("user_id"),
                resultSet.getObject("expires_at", OffsetDateTime.class)
        ), tokenHash);
        return invitations.stream().findFirst();
    }

    public boolean existsUnusedByHash(String tokenHash) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT count(*) FROM mottainai.employee_invitation_token
                 WHERE token_hash = ? AND used_at IS NULL AND expires_at > CURRENT_TIMESTAMP
                """, Integer.class, tokenHash);
        return count != null && count == 1;
    }

    public void markUsed(UUID invitationId) {
        jdbcTemplate.update("""
                UPDATE mottainai.employee_invitation_token SET used_at = CURRENT_TIMESTAMP
                 WHERE invitation_id = ? AND used_at IS NULL
                """, invitationId);
    }

    public void invalidateUnusedForUser(Integer userId) {
        jdbcTemplate.update("""
                UPDATE mottainai.employee_invitation_token SET used_at = CURRENT_TIMESTAMP
                 WHERE user_id = ? AND used_at IS NULL
                """, userId);
    }

    public boolean hasPendingForUser(Integer userId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT count(*) FROM mottainai.employee_invitation_token
                 WHERE user_id = ? AND used_at IS NULL
                """, Integer.class, userId);
        return count != null && count > 0;
    }

    public boolean hasRecentForUser(Integer userId, OffsetDateTime since) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT count(*) FROM mottainai.employee_invitation_token
                 WHERE user_id = ? AND created_at >= ?
                """, Integer.class, userId, since);
        return count != null && count > 0;
    }

    public record Invitation(UUID id, Integer userId, OffsetDateTime expiresAt) {
    }
}
