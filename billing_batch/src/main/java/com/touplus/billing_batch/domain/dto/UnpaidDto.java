package com.touplus.billing_batch.domain.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnpaidDto {

    private Long unpaidId;

    private Long userId;

    private Integer unpaidPrice;

    private LocalDate unpaidMonth;

    private Boolean paid;

    // Entity -> DTO 변환
    public static UnpaidDto fromEntity(com.touplus.billing_batch.domain.entity.Unpaid entity) {
        return UnpaidDto.builder()
                .unpaidId(entity.getUnpaidId())
                .userId(entity.getUserId())
                .unpaidPrice(entity.getUnpaidPrice())
                .unpaidMonth(entity.getUnpaidMonth())
                .paid(entity.getPaid())
                .build();
    }

    // DTO -> Entity 변환
    public com.touplus.billing_batch.domain.entity.Unpaid toEntity() {
        return com.touplus.billing_batch.domain.entity.Unpaid.builder()
                .unpaidId(this.unpaidId)
                .userId(this.userId)
                .unpaidPrice(this.unpaidPrice)
                .unpaidMonth(this.unpaidMonth)
                .paid(this.paid)
                .build();
    }
}
