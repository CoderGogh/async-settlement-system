package com.touplus.billing_api.admin.service.impl;

import com.touplus.billing_api.admin.dto.BillingProductStatResponse;
import com.touplus.billing_api.admin.service.BillingProductReportService;
import com.touplus.billing_api.domain.repository.billing.BillingProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/billing/products")
@RequiredArgsConstructor
public class BillingProductReportServiceImpl implements BillingProductReportService {
    private final BillingProductRepository billingProductRepository;

    @Override
    public List<BillingProductStatResponse> getTopSubscribedProducts(List<String> productTypes, int limit) {
        return billingProductRepository.findTopSubscribedByProductType(productTypes, limit);
    }
}
