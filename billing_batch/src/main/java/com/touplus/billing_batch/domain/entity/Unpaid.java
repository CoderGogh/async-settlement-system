package com.touplus.billing_batch.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "unpaid", indexes = {
        @Index(name = "idx_unpaid_month_paid", columnList = "unpaid_month, is_paid"),
        @Index(name = "idx_unpaid_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unpaid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long unpaidId;

    private Long userId;

    private Integer unpaidPrice;

    private LocalDate unpaidMonth;

    private Boolean paid = false;
}
