package com.touplus.billing_api.admin.controller;

import com.touplus.billing_api.admin.dto.BillingProductStatResponse;
import com.touplus.billing_api.admin.service.BillingProductReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class BillingProductReportController {

    private final BillingProductReportService billingProductReportService;

    /**
     * 특정 상품 타입 기준 Top 구독 상품 조회
     * GET /admin/products/top?types=MOVIE,TV&limit=10
     */
    @GetMapping("/top")
    public List<BillingProductStatResponse> getTopSubscribedProducts(
            @RequestParam List<String> types,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return billingProductReportService.getTopSubscribedProducts(types, limit);
    }
}
