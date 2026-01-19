package com.touplus.billing_message.domain.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.touplus.billing_message.domain.entity.BillingSnapshot;

public interface SnapshotDBRepository extends JpaRepository<BillingSnapshot, Long> {

	// snapshot 개수 알기 위한 쿼리
    @Query(value = "SELECT COUNT(*) FROM billing_message.billing_snapshot", nativeQuery = true)
    Long countAll();
}