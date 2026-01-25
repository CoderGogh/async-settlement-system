package com.touplus.billing_message.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis ZSet 기반 대기열 서비스
 * - score = 발송 예정 시간 (epoch seconds)
 * - value = messageId
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WaitingQueueService {

    private final StringRedisTemplate redisTemplate;
    private static final String QUEUE_KEY = "queue:message:waiting";
    private static final RedisScript<List> POP_READY_SCRIPT;

    static {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setResultType(List.class);
        script.setScriptText(
                "local key = KEYS[1]\n"
                        + "local maxScore = ARGV[1]\n"
                        + "local limit = tonumber(ARGV[2])\n"
                        + "if limit <= 0 then return {} end\n"
                        + "local ids = redis.call('ZRANGEBYSCORE', key, '-inf', maxScore, 'LIMIT', 0, limit)\n"
                        + "if #ids > 0 then\n"
                        + "  redis.call('ZREM', key, unpack(ids))\n"
                        + "end\n"
                        + "return ids\n");
        POP_READY_SCRIPT = script;
    }

    /**
     * 대기열에 메시지 추가
     * @param messageId 메시지 ID
     * @param scheduledAt 발송 예정 시간 (null이면 즉시 발송)
     */
    public void addToQueue(Long messageId, LocalDateTime scheduledAt) {
        LocalDateTime releaseTime = scheduledAt != null ? scheduledAt : LocalDateTime.now();
        long score = releaseTime.atZone(ZoneId.systemDefault()).toEpochSecond();

        String value = String.valueOf(messageId);
        redisTemplate.opsForZSet().add(QUEUE_KEY, value, score);

        log.debug("Redis 큐 추가: messageId={}, scheduledAt={}", messageId, releaseTime);
    }

    /**
     * 큐 기반 지연(초) 적용
     */
    public void addToQueue(Long messageId, LocalDateTime scheduledAt, long delaySeconds) {
        LocalDateTime releaseTime = scheduledAt != null ? scheduledAt : LocalDateTime.now();
        if (delaySeconds > 0) {
            releaseTime = releaseTime.plusSeconds(delaySeconds);
        }
        long score = releaseTime.atZone(ZoneId.systemDefault()).toEpochSecond();

        String value = String.valueOf(messageId);
        redisTemplate.opsForZSet().add(QUEUE_KEY, value, score);

        log.debug("Redis 큐 추가(delay={}s): messageId={}, scheduledAt={}",
                delaySeconds, messageId, releaseTime);
    }

    /**
     * Redis Pipeline으로 대기열에 메시지 일괄 추가
     * - N번 네트워크 왕복 → 1번으로 감소
     * @param messageIdScheduledAtMap messageId → scheduledAt 매핑
     * @param delaySeconds 추가 지연 시간 (초)
     */
    public void addToQueueBatch(Map<Long, LocalDateTime> messageIdScheduledAtMap, long delaySeconds) {
        if (messageIdScheduledAtMap == null || messageIdScheduledAtMap.isEmpty()) {
            return;
        }

        byte[] keyBytes = QUEUE_KEY.getBytes();

        redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                for (Map.Entry<Long, LocalDateTime> entry : messageIdScheduledAtMap.entrySet()) {
                    Long messageId = entry.getKey();
                    LocalDateTime scheduledAt = entry.getValue();

                    LocalDateTime releaseTime = scheduledAt != null ? scheduledAt : LocalDateTime.now();
                    if (delaySeconds > 0) {
                        releaseTime = releaseTime.plusSeconds(delaySeconds);
                    }
                    double score = releaseTime.atZone(ZoneId.systemDefault()).toEpochSecond();

                    connection.zSetCommands().zAdd(keyBytes, score, String.valueOf(messageId).getBytes());
                }
                return null;
            }
        });

        log.debug("Redis Pipeline 큐 추가: {}건, delay={}s", messageIdScheduledAtMap.size(), delaySeconds);
    }

    /**
     * 큐 맨 뒤로 추가하고 적용된 예정 시간을 반환
     */
    public LocalDateTime addToQueueTail(Long messageId) {
        long now = System.currentTimeMillis() / 1000;
        double score = now;

        Set<ZSetOperations.TypedTuple<String>> last =
                redisTemplate.opsForZSet().reverseRangeWithScores(QUEUE_KEY, 0, 0);
        if (last != null && !last.isEmpty()) {
            ZSetOperations.TypedTuple<String> tuple = last.iterator().next();
            if (tuple != null && tuple.getScore() != null && tuple.getScore() >= score) {
                score = tuple.getScore() + 1;
            }
        }

        redisTemplate.opsForZSet().add(QUEUE_KEY, String.valueOf(messageId), score);
        LocalDateTime scheduledAt = LocalDateTime.ofInstant(
                Instant.ofEpochSecond((long) score),
                ZoneId.systemDefault());

        log.debug("Redis 큐 꼬리 추가: messageId={}, scheduledAt={}", messageId, scheduledAt);
        return scheduledAt;
    }

    /**
     * 큐 맨 뒤로 일괄 추가 (Pipeline) + 적용된 scheduledAt 반환
     * - 실패 메시지 재큐잉용
     */
    public LocalDateTime addToQueueTailBatch(List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return LocalDateTime.now();
        }

        // 현재 큐의 마지막 스코어 조회
        long now = System.currentTimeMillis() / 1000;
        double baseScore = now;

        Set<ZSetOperations.TypedTuple<String>> last =
                redisTemplate.opsForZSet().reverseRangeWithScores(QUEUE_KEY, 0, 0);
        if (last != null && !last.isEmpty()) {
            ZSetOperations.TypedTuple<String> tuple = last.iterator().next();
            if (tuple != null && tuple.getScore() != null && tuple.getScore() >= baseScore) {
                baseScore = tuple.getScore() + 1;
            }
        }

        final double startScore = baseScore;
        byte[] keyBytes = QUEUE_KEY.getBytes();

        redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                double score = startScore;
                for (Long messageId : messageIds) {
                    connection.zSetCommands().zAdd(keyBytes, score, String.valueOf(messageId).getBytes());
                    score += 1;  // 각 메시지마다 1초씩 증가
                }
                return null;
            }
        });

        LocalDateTime scheduledAt = LocalDateTime.ofInstant(
                Instant.ofEpochSecond((long) startScore),
                ZoneId.systemDefault());

        log.debug("Redis Pipeline 큐 꼬리 추가: {}건, startScheduledAt={}", messageIds.size(), scheduledAt);
        return scheduledAt;
    }

    /**
     * 발송 가능한 메시지 ID 조회 (현재 시간 이전)
     * @param limit 최대 조회 건수
     * @return 발송 가능한 메시지 ID 목록
     */
    public Set<String> getReadyMessageIds(int limit) {
        long now = System.currentTimeMillis() / 1000;
        return redisTemplate.opsForZSet().rangeByScore(QUEUE_KEY, 0, now, 0, limit);
    }

    /**
     * ZPOPMIN으로 원자적으로 가져온 뒤, 현재 시간 이전만 반환
     * - Lua로 현재 시간 이전만 원자적으로 pop
     */
    public List<String> popReadyMessageIds(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }

        long now = System.currentTimeMillis() / 1000;
        List<String> result = redisTemplate.execute(
                POP_READY_SCRIPT,
                Collections.singletonList(QUEUE_KEY),
                String.valueOf(now),
                String.valueOf(limit));

        return result != null ? result : Collections.emptyList();
    }

    /**
     * 대기열에서 메시지 제거
     */
    public void removeFromQueue(Long messageId) {
        redisTemplate.opsForZSet().remove(QUEUE_KEY, String.valueOf(messageId));
    }

    /**
     * 대기열에서 메시지 제거 (String)
     */
    public void removeFromQueue(String messageId) {
        redisTemplate.opsForZSet().remove(QUEUE_KEY, messageId);
    }

    /**
     * 대기열 전체 크기
     */
    public Long getQueueSize() {
        return redisTemplate.opsForZSet().size(QUEUE_KEY);
    }

    /**
     * 발송 가능한 메시지 수 (현재 시간 이전)
     */
    public Long getReadyCount() {
        long now = System.currentTimeMillis() / 1000;
        return redisTemplate.opsForZSet().count(QUEUE_KEY, 0, now);
    }

    /**
     * 대기열 초기화
     */
    public void clearQueue() {
        redisTemplate.delete(QUEUE_KEY);
        log.info("Redis 큐 초기화 완료: key={}", QUEUE_KEY);
    }
}
