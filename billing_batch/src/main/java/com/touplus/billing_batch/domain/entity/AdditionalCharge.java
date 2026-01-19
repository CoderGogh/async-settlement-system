package com.touplus.billing_batch.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "additional_charge", indexes = {
        @Index(name = "idx_additional_user_month", columnList = "user_id, additional_charge_month")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdditionalCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;

    private Integer price;

    private LocalDate additionalChargeMonth;

    private Long userId;
}
