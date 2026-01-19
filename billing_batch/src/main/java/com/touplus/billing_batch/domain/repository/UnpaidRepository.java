package com.touplus.billing_batch.domain.repository;

import com.touplus.billing_batch.domain.entity.Unpaid;
import com.touplus.billing_batch.domain.entity.BillingUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface UnpaidRepository extends JpaRepository<Unpaid, Long> {
    List<Unpaid> findByUser(BillingUser user);
    List<Unpaid> findByPaidFalseAndUnpaidMonthBefore(LocalDate month);
}
