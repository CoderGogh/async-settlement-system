package com.touplus.billing_batch.domain.dto;

import com.touplus.billing_batch.domain.entity.BillingUser;
import com.touplus.billing_batch.domain.entity.Unpaid;
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
    public static UnpaidDto fromEntity(Unpaid entity) {
        return UnpaidDto.builder()
                .unpaidId(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getUserId() : null)
                .unpaidPrice(entity.getUnpaidPrice())
                .unpaidMonth(entity.getUnpaidMonth())
                .paid(entity.getPaid() != null ? entity.getPaid() : Boolean.FALSE)
                .build();
    }

    // DTO -> Entity 변환
    public Unpaid toEntity(BillingUser user) {
        return Unpaid.builder()
                .id(this.unpaidId)
                .user(user)
                .unpaidPrice(this.unpaidPrice)
                .unpaidMonth(this.unpaidMonth)
                .paid(this.paid != null ? this.paid : Boolean.FALSE)
                .build();
    }
}
