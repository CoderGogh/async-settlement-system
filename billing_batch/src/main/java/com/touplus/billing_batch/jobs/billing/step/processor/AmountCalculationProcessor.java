package com.touplus.billing_batch.jobs.billing.step.processor;

import com.touplus.billing_batch.domain.dto.*;
import com.touplus.billing_batch.domain.entity.AdditionalCharge;
import com.touplus.billing_batch.domain.entity.UserSubscribeProduct;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class AmountCalculationProcessor
        implements ItemProcessor<BillingUserBillingInfoDto, BillingWorkDto> {

    @Override
    public BillingWorkDto process(BillingUserBillingInfoDto item) throws Exception {

        // 상품 가격 합산
        int productSum = item.getProducts().stream()
                .mapToInt(UserSubscribeProductDto::getPrice)
                .sum();

        // 추가 요금 합산
        int additionalChargeSum = item.getAdditionalCharges().stream()
                .mapToInt(AdditionalChargeDto::getPrice)
                .sum();

        // 총 상품 금액 + 총 추가요금

        return BillingWorkDto.builder()
                .rawData(item)
                .productAmount(productSum)
                .additionalCharges(additionalChargeSum)
                .baseAmount(productSum + additionalChargeSum)
                .build();
    }

}
