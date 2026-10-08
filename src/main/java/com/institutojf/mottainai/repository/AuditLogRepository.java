package com.institutojf.mottainai.repository;

import com.institutojf.mottainai.dto.response.AuditLogResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AuditLogRepository {
    private static final String INSERT_AUDIT_LOG = """
            INSERT INTO mottainai.audit_log (table_affected, operation, record_id, user_id, old_data, new_data)
            VALUES (?, CAST(? AS mottainai.audit_operation), ?, ?, CAST(? AS JSONB), CAST(? AS JSONB))
            """;

    private final JdbcTemplate jdbcTemplate;
    private final JsonMapper jsonMapper;

    public AuditLogRepository(JdbcTemplate jdbcTemplate, JsonMapper jsonMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonMapper = jsonMapper;
    }

    public void record(String table, String operation, String recordId, Integer actorUserId,
                       Object oldData, Object newData) {
        jdbcTemplate.update(INSERT_AUDIT_LOG, table, operation, recordId, actorUserId,
                toJson(oldData), toJson(newData));
    }

    public List<AuditLogResponse> findByEmployeeIdAndOperationDateBetween(
            Integer employeeId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        String sql = """
                SELECT audit.audit_id, audit.table_affected, audit.operation::TEXT AS operation,
                       audit.record_id, audit.user_id, audit.old_data::TEXT AS old_data,
                       audit.new_data::TEXT AS new_data, audit.operation_date
                FROM mottainai.audit_log audit
                JOIN mottainai.app_user app_user ON app_user.user_id = audit.user_id
                WHERE app_user.employee_id = ?
                  AND audit.operation_date >= ?
                  AND audit.operation_date <= ?
                ORDER BY audit.operation_date DESC, audit.audit_id DESC
                """;
        return jdbcTemplate.query(sql, auditLogRowMapper(), employeeId, from, to);
    }

    private RowMapper<AuditLogResponse> auditLogRowMapper() {
        return (resultSet, rowNumber) -> new AuditLogResponse(
                resultSet.getLong("audit_id"),
                resultSet.getString("table_affected"),
                resultSet.getString("operation"),
                resultSet.getString("record_id"),
                resultSet.getObject("user_id", Integer.class),
                readJson(resultSet, "old_data"),
                readJson(resultSet, "new_data"),
                resultSet.getTimestamp("operation_date").toLocalDateTime()
        );
    }

    private JsonNode readJson(ResultSet resultSet, String column) throws SQLException {
        String json = resultSet.getString(column);
        if (json == null) {
            return null;
        }
        try {
            return jsonMapper.readTree(json);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Audit log contains invalid JSON", exception);
        }
    }

    private String toJson(Object data) {
        if (data == null) {
            return null;
        }
        try {
            return jsonMapper.writeValueAsString(data);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Could not serialize audit data", exception);
        }
    }
}
