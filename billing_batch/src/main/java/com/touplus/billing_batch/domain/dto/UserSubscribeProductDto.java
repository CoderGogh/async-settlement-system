package com.touplus.billing_batch.domain.dto;

import com.touplus.billing_batch.domain.entity.BillingProduct;
import com.touplus.billing_batch.domain.entity.BillingUser;
import com.touplus.billing_batch.domain.entity.UserSubscribeProduct;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscribeProductDto {

    private Long userSubscribeProductId;
    private LocalDate createdMonth;
    private LocalDateTime deletedAt;
    private Long userId;
    private Long productId;

    // Entity -> DTO 변환
    public static UserSubscribeProductDto fromEntity(UserSubscribeProduct entity) {
        return UserSubscribeProductDto.builder()
                .userSubscribeProductId(entity.getUserSubscribeProductId())
                .createdMonth(entity.getCreatedMonth())
                .deletedAt(entity.getDeletedAt())
                .userId(entity.getUser() != null ? entity.getUser().getUserId() : null)
                .productId(entity.getProduct() != null ? entity.getProduct().getProductId() : null)
                .build();
    }

    // DTO -> Entity 변환
    public UserSubscribeProduct toEntity() {
        BillingUser user = null;
        if (this.userId != null) {
            user = BillingUser.builder().userId(this.userId).build();
        }

        BillingProduct product = null;
        if (this.productId != null) {
            product = BillingProduct.builder().productId(this.productId).build();
        }

        return UserSubscribeProduct.builder()
                .userSubscribeProductId(this.userSubscribeProductId)
                .createdMonth(this.createdMonth)
                .deletedAt(this.deletedAt)
                .user(user)
                .product(product)
                .build();
    }
}
