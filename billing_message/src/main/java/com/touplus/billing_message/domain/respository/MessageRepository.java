package com.touplus.billing_message.domain.respository;

import com.touplus.billing_message.domain.entity.Message;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query(
        value = """
            SELECT message_id
            FROM message
            WHERE status IN ('CREATED','WAITED')
              AND (scheduled_at IS NULL OR scheduled_at <= :now)
            ORDER BY (scheduled_at IS NULL), scheduled_at, message_id
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
        """,
        nativeQuery = true
    )
    List<Long> lockNextMessageIds(@Param("now") LocalDateTime now,
                                  @Param("limit") int limit);

    @Modifying
    @Query(
        value = """
            UPDATE message
            SET status = 'SENT'
            WHERE message_id IN (:ids)
              AND status IN ('CREATED','WAITED')
        """,
        nativeQuery = true
    )
    int markSentByIds(@Param("ids") List<Long> ids);

    @Modifying
    @Query(
        value = """
            UPDATE message
            SET status = 'SENT'
            WHERE message_id = :id
        """,
        nativeQuery = true
    )
    int markSent(@Param("id") Long id);

    @Modifying
    @Query(
        value = """
            UPDATE message
            SET status = 'WAITED',
                scheduled_at = :scheduledAt
            WHERE message_id = :id
        """,
        nativeQuery = true
    )
    int defer(@Param("id") Long id,
              @Param("scheduledAt") LocalDateTime scheduledAt);

    @Modifying
    @Query(
        value = """
            UPDATE message
            SET status = 'WAITED',
                retry_count = retry_count + 1,
                scheduled_at = :scheduledAt
            WHERE message_id = :id
        """,
        nativeQuery = true
    )
    int markFailed(@Param("id") Long id,
                   @Param("scheduledAt") LocalDateTime scheduledAt);
}
