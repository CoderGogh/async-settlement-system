package com.touplus.billing_batch.domain.repository;

import com.touplus.billing_batch.domain.entity.BillingUser;
import com.touplus.billing_batch.domain.entity.Unpaid;

import java.time.LocalDate;
import java.util.List;

public interface UnpaidRepository {

    List<Unpaid> findByUser(BillingUser user);

    List<Unpaid> findByPaidFalseAndUnpaidMonthBefore(LocalDate month);

    List<Unpaid> findByUserIdIn(List<Long> userIds);
}
