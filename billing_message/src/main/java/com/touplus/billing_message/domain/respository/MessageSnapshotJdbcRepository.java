package com.touplus.billing_message.domain.respository;

import com.touplus.billing_message.domain.entity.MessageSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MessageSnapshotJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 메시지 스냅샷 Batch INSERT (INSERT IGNORE)
     * - JPA saveAll 대비 10배 이상 빠름
     */
    public int batchInsert(List<MessageSnapshot> snapshots) {
        if (snapshots.isEmpty()) {
            return 0;
        }

        String sql = """
            INSERT IGNORE INTO message_snapshot
            (message_id, billing_id, settlement_month, user_id, user_name,
             user_email, user_phone, total_price, settlement_details, message_content)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        int[][] results = jdbcTemplate.batchUpdate(
            sql,
            snapshots,
            snapshots.size(),
            (ps, snapshot) -> {
                ps.setLong(1, snapshot.getMessageId());
                ps.setLong(2, snapshot.getBillingId());
                ps.setDate(3, snapshot.getSettlementMonth() != null
                    ? Date.valueOf(snapshot.getSettlementMonth()) : null);
                ps.setLong(4, snapshot.getUserId());
                ps.setString(5, snapshot.getUserName());
                ps.setString(6, snapshot.getUserEmail());
                ps.setString(7, snapshot.getUserPhone());
                ps.setInt(8, snapshot.getTotalPrice());
                ps.setString(9, snapshot.getSettlementDetails());
                ps.setString(10, snapshot.getMessageContent());
            }
        );

        int successCount = 0;
        for (int[] batch : results) {
            for (int affected : batch) {
                if (affected > 0 || affected == java.sql.Statement.SUCCESS_NO_INFO) {
                    successCount++;
                }
            }
        }
        return successCount;
    }

    /**
     * 메시지 스냅샷 Bulk 조회 (JDBC)
     */
    public List<MessageSnapshotDto> findByIds(List<Long> messageIds) {
        if (messageIds.isEmpty()) {
            return Collections.emptyList();
        }

        String placeholders = messageIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String sql = """
                SELECT message_id, billing_id, settlement_month, user_id, user_name,
                       user_email, user_phone, total_price, settlement_details, message_content
                FROM message_snapshot WHERE message_id IN (%s)
                """.formatted(placeholders);

        return jdbcTemplate.query(sql, messageIds.toArray(), (rs, rowNum) -> new MessageSnapshotDto(
                rs.getLong("message_id"),
                rs.getLong("billing_id"),
                rs.getObject("settlement_month", LocalDate.class),
                rs.getLong("user_id"),
                rs.getString("user_name"),
                rs.getString("user_email"),
                rs.getString("user_phone"),
                rs.getInt("total_price"),
                rs.getString("settlement_details"),
                rs.getString("message_content")));
    }

    /**
     * 메시지 스냅샷 단건 조회 (JDBC)
     */
    public MessageSnapshotDto findById(Long messageId) {
        String sql = """
                    SELECT message_id, billing_id, settlement_month, user_id, user_name,
                           user_email, user_phone, total_price, settlement_details, message_content
                    FROM message_snapshot WHERE message_id = ?
                """;

        List<MessageSnapshotDto> results = jdbcTemplate.query(sql, (rs, rowNum) -> new MessageSnapshotDto(
                rs.getLong("message_id"),
                rs.getLong("billing_id"),
                rs.getObject("settlement_month", LocalDate.class),
                rs.getLong("user_id"),
                rs.getString("user_name"),
                rs.getString("user_email"),
                rs.getString("user_phone"),
                rs.getInt("total_price"),
                rs.getString("settlement_details"),
                rs.getString("message_content")), messageId);

        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 메시지 스냅샷 DTO
     */
    public record MessageSnapshotDto(
            Long messageId,
            Long billingId,
            LocalDate settlementMonth,
            Long userId,
            String userName,
            String userEmail,
            String userPhone,
            Integer totalPrice,
            String settlementDetails,
            String messageContent) {
    }
}
