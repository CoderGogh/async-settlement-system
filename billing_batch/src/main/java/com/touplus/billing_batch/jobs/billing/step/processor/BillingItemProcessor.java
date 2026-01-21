package com.touplus.billing_batch.jobs.billing.step.processor;

import java.util.List;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.touplus.billing_batch.domain.dto.BillingCalculationResult;
import com.touplus.billing_batch.domain.dto.BillingUserBillingInfoDto;
import com.touplus.billing_batch.domain.entity.BillingProduct;
import com.touplus.billing_batch.common.BillingException;

@Component
public class BillingItemProcessor
        implements ItemProcessor<BillingUserBillingInfoDto, BillingCalculationResult> { // 타입을 DTO로 변경

    @Override
    public BillingCalculationResult process(BillingUserBillingInfoDto item) {

        // 1. 이미 Reader에서 조회해온 구독 상품 리스트 추출
        // Reader가 이미 BillingProduct 정보를 포함한 UserSubscribeProduct 리스트를 넘겨줍니다.
        List<BillingProduct> products = item.getProducts().stream()
                .map(usp -> usp.getProduct()) // UserSubscribeProduct 엔티티 내의 Product 객체 추출
                .toList();

        // 2. 구독 중인 상품이 없는 경우 예외 처리
        if (products.isEmpty()) {
            throw BillingException.dataNotFound(item.getUserId());
        }

        // 3. 합계 금액 계산 (상품 가격 + 추가 요금 - 할인 등 로직 확장 가능)
        int totalPrice = products.stream()
                .mapToInt(BillingProduct::getPrice)
                .sum();

        // [추가 요금 반영 예시]
        int totalAdditionalCharge = item.getAdditionalCharges().stream()
                .mapToInt(ac -> ac.getPrice())
                .sum();

        totalPrice += totalAdditionalCharge;

        // 4. 금액 검증
        if (totalPrice < 0) {
            throw BillingException.invalidAmount(item.getUserId());
        }

        return new BillingCalculationResult(
                item.getUserId(),
                totalPrice,
                products
        );
    }
}