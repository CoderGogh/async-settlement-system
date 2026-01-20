package com.touplus.billing_batch.domain.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingUserBillingInfoDto {

    private Long userId;

    @Builder.Default
    private List<UserSubscribeProductDto> products = new ArrayList<>();
    @Builder.Default
    private List<UnpaidDto> unpaids = new ArrayList<>();
    @Builder.Default
    private List<AdditionalChargeDto> additionalCharges = new ArrayList<>();
    @Builder.Default
    private List<UserSubscribeDiscountDto> discounts = new ArrayList<>();
}