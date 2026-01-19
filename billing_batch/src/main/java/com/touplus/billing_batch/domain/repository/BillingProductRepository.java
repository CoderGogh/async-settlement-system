package com.touplus.billing_batch.domain.repository;

import com.touplus.billing_batch.domain.entity.BillingProduct;

import java.util.List;

public interface BillingProductRepository {

    List<BillingProduct> findAll();

    BillingProduct findById(Long id);

    List<BillingProduct> findByIdIn(List<Long> productIds);
}
