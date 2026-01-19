package com.touplus.billing_batch.domain.dto;

import com.touplus.billing_batch.domain.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingProductDto {
    private Long productId;
    private String productName;
    private ProductType productType;
    private Integer price;
}