package com.touplus.billing_batch.domain.dto;

import com.touplus.billing_batch.domain.entity.BillingDiscount;
import com.touplus.billing_batch.domain.entity.BillingProduct;
import com.touplus.billing_batch.domain.entity.BillingUser;
import com.touplus.billing_batch.domain.entity.UserSubscribeDiscount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscribeDiscountDto {
    private Long udsId;
    private LocalDate discountSubscribeMonth;
    private Long userId;
    private Long discountId;
    private Long productId;

    public static UserSubscribeDiscountDto fromEntity(UserSubscribeDiscount entity) {
        return UserSubscribeDiscountDto.builder()
                .udsId(entity.getUdsId())
                .discountSubscribeMonth(entity.getDiscountSubscribeMonth())
                .userId(entity.getBillingUser() != null ? entity.getBillingUser().getUserId() : null)
                .discountId(entity.getBillingDiscount() != null ? entity.getBillingDiscount().getDiscountId() : null)
                .productId(entity.getBillingProduct() != null ? entity.getBillingProduct().getProductId() : null)
                .build();
    }

    public UserSubscribeDiscount toEntity(BillingUser user, BillingDiscount discount, BillingProduct product) {
        return UserSubscribeDiscount.builder()
                .udsId(this.udsId)
                .discountSubscribeMonth(this.discountSubscribeMonth)
                .billingUser(user)
                .billingDiscount(discount)
                .billingProduct(product)
                .build();
    }
}
