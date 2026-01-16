package com.touplus.billing_message.domain.respository;

import com.touplus.billing_message.domain.entity.BillingSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingSnapshotRepository
        extends JpaRepository<BillingSnapshot, Long> {
}
