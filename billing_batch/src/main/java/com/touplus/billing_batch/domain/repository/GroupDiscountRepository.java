package com.touplus.billing_batch.domain.repository;

import com.touplus.billing_batch.domain.entity.GroupDiscount;

import java.util.Optional;

public interface GroupDiscountRepository {

    Optional<GroupDiscount> findByGroupId(Long groupId);
}
