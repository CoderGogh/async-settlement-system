package com.touplus.billing_batch.domain.repository;

import com.touplus.billing_batch.domain.entity.ProductBaseUsage;
import java.util.List;

public interface ProductBaseUsageRepository {
    List<ProductBaseUsage> findByProductId(Long productId);
    List<ProductBaseUsage> findAll();
}