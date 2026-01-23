package com.touplus.billing_api.admin.dto;

import com.touplus.billing_api.domain.billing.enums.ProductType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingProductStatResponse {
    private Long productId;           // 상품 ID
    private String productName;       // 상품명
    private ProductType productType;  // 상품 타입
    private Integer price;            // 상품 가격
    private Long subscribeCount;      // 구독자 수
}
