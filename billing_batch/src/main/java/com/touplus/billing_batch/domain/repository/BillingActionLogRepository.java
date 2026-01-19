package com.touplus.billing_batch.domain.repository;

import com.touplus.billing_batch.domain.entity.BillingActionLog;

public interface BillingActionLogRepository {

    // 액션 로그 저장
    void save(BillingActionLog actionLog);
}
