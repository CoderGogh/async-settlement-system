package com.touplus.billing_batch.domain.repository;

import com.touplus.billing_batch.domain.entity.BillingActionLog;
import com.touplus.billing_batch.domain.enums.ActionResult;
import com.touplus.billing_batch.domain.enums.ActionType;
import com.touplus.billing_batch.domain.enums.ActorType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class BillingActionLogRepositoryImpl
        implements BillingActionLogRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    /**
     * INSERT ONLY
     */
    @Override
    public void save(BillingActionLog actionLog) {

        String sql = """
            INSERT INTO batch_billing_action_log (
                error_log_id,
                actor_type,
                actor_id,
                action_type,
                action_message,
                action_result,
                action_at
            ) VALUES (
                :errorLogId,
                :actorType,
                :actorId,
                :actionType,
                :actionMessage,
                :actionResult,
                :actionAt
            )
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("errorLogId", actionLog.getErrorLogId())
                .addValue("actorType", actionLog.getActorType().name())
                .addValue("actorId", actionLog.getActorId())
                .addValue("actionType", actionLog.getActionType().name())
                .addValue("actionMessage", actionLog.getActionMessage())
                .addValue("actionResult", actionLog.getActionResult().name())
                .addValue("actionAt", actionLog.getActionAt());

        namedJdbcTemplate.update(sql, params);
    }
}
