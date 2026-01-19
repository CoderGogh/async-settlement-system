package com.touplus.billing_batch.domain.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdditionalChargeDto {

    private Long id;

    private String companyName;

    private Integer price;

    private LocalDate additionalChargeMonth;

    private Long userId;

    // Entity -> DTO 변환
    public static AdditionalChargeDto fromEntity(com.touplus.billing_batch.domain.entity.AdditionalCharge entity) {
        return AdditionalChargeDto.builder()
                .id(entity.getId())
                .companyName(entity.getCompanyName())
                .price(entity.getPrice())
                .additionalChargeMonth(entity.getAdditionalChargeMonth())
                .userId(entity.getUserId())
                .build();
    }

    // DTO -> Entity 변환
    public com.touplus.billing_batch.domain.entity.AdditionalCharge toEntity() {
        return com.touplus.billing_batch.domain.entity.AdditionalCharge.builder()
                .id(this.id)
                .companyName(this.companyName)
                .price(this.price)
                .additionalChargeMonth(this.additionalChargeMonth)
                .userId(this.userId)
                .build();
    }
}
