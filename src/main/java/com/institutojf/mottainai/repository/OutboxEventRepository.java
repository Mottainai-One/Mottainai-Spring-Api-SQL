package com.institutojf.mottainai.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OutboxEventRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JsonMapper jsonMapper;

    public void lockIdempotencyKey(String idempotencyKey) {
        jdbcTemplate.execute(
                "SELECT pg_advisory_xact_lock(hashtextextended(?, 0))",
                (PreparedStatementCallback<Void>) statement -> {
                    statement.setString(1, idempotencyKey);
                    statement.execute();
                    return null;
                }
        );
    }

    public Optional<Event> findByIdempotencyKey(String idempotencyKey) {
        return jdbcTemplate.query(
                "SELECT event_type, aggregate_type, aggregate_id, event_data::text "
                        + "FROM mottainai.event_queue WHERE idempotency_key = ?",
                (ResultSetExtractor<Optional<Event>>) resultSet -> {
                    if (!resultSet.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(new Event(
                            resultSet.getString("event_type"),
                            resultSet.getString("aggregate_type"),
                            resultSet.getString("aggregate_id"),
                            resultSet.getString("event_data")
                    ));
                },
                idempotencyKey
        );
    }

    public boolean isLoyaltyRedemptionFor(Event event, Integer customerId, Integer rewardId) {
        if (!"LOYALTY_REDEMPTION".equals(event.eventType())
                || !"loyalty_redemption".equals(event.aggregateType())) {
            return false;
        }
        try {
            JsonNode eventData = jsonMapper.readTree(event.eventData());
            return eventData.path("customer_id").asInt(Integer.MIN_VALUE) == customerId
                    && eventData.path("reward_id").asInt(Integer.MIN_VALUE) == rewardId;
        } catch (JacksonException exception) {
            throw new IllegalStateException("Stored outbox event contains invalid JSON", exception);
        }
    }

    public void publishLoyaltyRedemption(String redemptionId, Integer customerId, Integer rewardId,
                                         Integer pointsSpent, String idempotencyKey) {
        String eventData;
        try {
            eventData = jsonMapper.writeValueAsString(Map.of(
                    "redemption_id", redemptionId,
                    "customer_id", customerId,
                    "reward_id", rewardId,
                    "points_spent", pointsSpent
            ));
        } catch (JacksonException exception) {
            throw new IllegalStateException("Could not serialize loyalty redemption event", exception);
        }

        jdbcTemplate.queryForObject(
                "SELECT mottainai.fn_publish_integration_event(?, ?, ?, CAST(? AS jsonb), NULL, NULL, ?, 5)",
                Object.class,
                "LOYALTY_REDEMPTION",
                "loyalty_redemption",
                redemptionId,
                eventData,
                idempotencyKey
        );
    }

    public record Event(
            String eventType,
            String aggregateType,
            String aggregateId,
            String eventData
    ) {
    }
}
