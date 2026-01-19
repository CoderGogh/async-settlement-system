package com.touplus.billing_batch.jobs.billing.step.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.touplus.billing_batch.domain.dto.*;
import com.touplus.billing_batch.domain.dto.SettlementDetailsDto.DetailItem;
import com.touplus.billing_batch.domain.enums.ProductType;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@StepScope
@RequiredArgsConstructor
public class FinalBillingResultProcessor
        implements ItemProcessor<BillingWorkDto, BillingCalculationResult> {

    private final ObjectMapper objectMapper;

    @Override
    public BillingCalculationResult process(BillingWorkDto work) throws Exception {
        // 미납금 합산
        int totalUnpaid = work.getRawData().getUnpaids().stream()
                .mapToInt(UnpaidDto::getUnpaidPrice)
                .sum();

        // 최종 청구 금액 계산 (상품 + 추가요금 - 할인 + 미납금)
        int finalPrice = work.getTotalPrice() + totalUnpaid;

        // 상세 내역 구성
        SettlementDetailsDto settlementDetailsDto = makeSettlementDetails(work.getRawData());
        String detailsJson = objectMapper.writeValueAsString(settlementDetailsDto);

        return null;
    }

    public SettlementDetailsDto makeSettlementDetails(BillingUserBillingInfoDto item){
        // 각 item을 담을 List 생성
        List<DetailItem> mobile = new ArrayList<>();
        List<DetailItem> internet = new ArrayList<>();
        List<DetailItem> iptv = new ArrayList<>();
        List<DetailItem> dps = new ArrayList<>();
        List<DetailItem> addon = new ArrayList<>();

        // 상품 담기
        for (UserSubscribeProductDto product : item.getProducts()) {
            ProductType productType = product.getProductType();

            DetailItem detail = DetailItem.builder()
                    .productType(productType.name())
                    .productName(product.getProductName())
                    .price(product.getPrice())
                    .build();

            switch (productType) {
                case mobile -> mobile.add(detail);
                case internet -> internet.add(detail);
                case iptv -> iptv.add(detail);
                case dps -> dps.add(detail);
                case addon -> addon.add(detail);
            }
        }

        // 추가요금 담기
        for(AdditionalChargeDto chargeDto : item.getAdditionalCharges()) {
            addon.add(DetailItem.builder()
                    .productType("additional_charge")
                    .productName(chargeDto.getCompanyName())
                    .build());
        }

        // 할인 내역 담기(상품과 매핑 필요 없음)

    }
}
