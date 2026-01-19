package com.touplus.billing_batch.domain.dto;

import com.touplus.billing_batch.domain.enums.ProductType;
import lombok.*;

import java.util.List;

@Getter
@Builder
public class SettlementDetailsDto {

    private List<DetailItem> mobile;      // 휴대폰 요금
    private List<DetailItem> internet;      // 인터넷 요금
    private List<DetailItem> iptv;      // tv
    private List<DetailItem> dps;      // 결합상품
    private List<DetailItem> addon;     // 부가 서비스
    private List<DetailItem> discounts; // 할인 내역
    private List<DetailItem> unpaids;   // 미납 내역

    @Getter
    @Builder
    public static class DetailItem {
        private String productType;
        private String productName;
        private Integer price;
    }
}
