package com.touplus.billing_api.domain.message.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MessageStatusSummaryDto {
    private long sentCount;
    private long failCount;
    private double sentRate;
    private double failRate;
    private long totalCount;
    private String settlementMonth;
}

