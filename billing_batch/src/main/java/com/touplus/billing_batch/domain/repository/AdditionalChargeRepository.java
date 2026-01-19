package com.touplus.billing_batch.domain.repository;

import com.touplus.billing_batch.domain.entity.AdditionalCharge;
import com.touplus.billing_batch.domain.entity.BillingUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AdditionalChargeRepository extends JpaRepository<AdditionalCharge, Long> {
    List<AdditionalCharge> findByUser(BillingUser user);
    List<AdditionalCharge> findByAdditionalChargeMonth(LocalDate month);
}
