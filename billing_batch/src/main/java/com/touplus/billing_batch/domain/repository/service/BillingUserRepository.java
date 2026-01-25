package com.touplus.billing_batch.domain.repository.service;

import com.touplus.billing_batch.domain.dto.BillingUserMemberDto;
import com.touplus.billing_batch.domain.dto.MinMaxIdDto;
import com.touplus.billing_batch.domain.entity.BillingUser;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BillingUserRepository {

    List<BillingUser> findUsersGreaterThanId(Long lastUserId, Pageable pageable);

    MinMaxIdDto findMinMaxId();
    List<BillingUserMemberDto> findUsersInRange(
            Long minValue,
            Long maxValue,
            Long lastProcessedUserId,
            boolean forceFullScan,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );
    List<BillingUser> findByUserIdIn(List<Long> userIds);
}
