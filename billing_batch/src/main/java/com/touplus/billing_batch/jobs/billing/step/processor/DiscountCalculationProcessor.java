package com.touplus.billing_batch.jobs.billing.step.processor;

import com.touplus.billing_batch.common.BillingException;
import com.touplus.billing_batch.common.BillingFatalException;
import com.touplus.billing_batch.domain.dto.BillingWorkDto;
import com.touplus.billing_batch.domain.dto.SettlementDetailsDto;
import com.touplus.billing_batch.domain.dto.UserSubscribeDiscountDto;
import com.touplus.billing_batch.domain.enums.CalOrderType;
import com.touplus.billing_batch.domain.enums.DiscountRangeType;
import com.touplus.billing_batch.jobs.billing.cache.BillingReferenceCache;
import com.touplus.billing_batch.domain.dto.*;
import com.touplus.billing_batch.domain.enums.DiscountType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class DiscountCalculationProcessor implements ItemProcessor<BillingWorkDto, BillingWorkDto> {

    private final BillingReferenceCache referenceCache;

    @Override
    public BillingWorkDto process(BillingWorkDto work) throws Exception {
        // 할인&상품 관련 데이터 가져오기
        Queue<UserSubscribeDiscountDto> discounts =
                work.getRawData().getDiscounts() == null
                        ? new LinkedList<>()
                        : new LinkedList<>(work.getRawData().getDiscounts());

        // 캐시 미리 가져오기
        Map<Long, BillingDiscountDto> discountMap = referenceCache.getDiscountMap();
        Map<Long, BillingProductDto>  productMap = referenceCache.getProductMap();
        Map<Long, DiscountPolicyDto> discountPolicyMap = referenceCache.getDiscountPolicyMap();

        if(discountMap == null || discountMap.isEmpty())
            throw BillingFatalException.cacheNotFound("할인 정보 캐싱이 이루어지지 않았습니다.");
        if(productMap == null || productMap.isEmpty())
            throw BillingFatalException.cacheNotFound("상품 정보 캐싱이 이루어지지 않았습니다.");
        if(discountMap == null || discountMap.isEmpty()){
            throw BillingFatalException.cacheNotFound("할인 정책 캐싱이 이루어지지 않았습니다.");
        }

//        log.info("[DiscountCalculationProcessor] 할인, 상품 정보 가져오기");
        
        // 할인금액 합산 변수
        long totalDiscount = 0;
        int joinedYear = work.getJoinedYear();
        int numOfMember = work.getRawData().getNumOfMember() == null ? 0 : work.getRawData().getNumOfMember();

        // 결합 할인을 위해 모바일/인터냇 보유 여부 확인용
        boolean hasMobile = false;
        boolean hasInternet = false;
        long mobilePrice = 0;
        long internetPrice = 0;

        while(!discounts.isEmpty()) {
            UserSubscribeDiscountDto usd = discounts.poll();

            // 캐시에서 상품 정보 가져오기
            BillingProductDto product = productMap.get(usd.getProductId());
            BillingDiscountDto discount = discountMap.get(usd.getDiscountId());
            DiscountPolicyDto discountPolicy = discountPolicyMap.get(usd.getDiscountId());

            if (product == null) {
                throw BillingException.dataNotFound(work.getRawData().getUserId(), "상품 정보(ID: " + usd.getProductId() + ")가 캐시에 없습니다.");
            }
            if (discount == null) {
                throw BillingException.dataNotFound(work.getRawData().getUserId(), "할인 정보(ID: " + usd.getDiscountId() + ")가 캐시에 없습니다.");
            }
            if(discountPolicy == null){
                throw BillingException.dataNotFound(work.getRawData().getUserId(), "할인 정책 정보(ID: " + usd.getDiscountId() + ")가 캐시에 없습니다.");
            }

            // 할인 데이터 이상
            if (discount.getIsCash() == null) {
                throw BillingException.dataNotFound(work.getRawData().getUserId(), "할인 타입이 비어있습니다.");
            }

            if (discount.getDiscountName() == null || discount.getDiscountName().isBlank()) {
                throw BillingException.dataNotFound(work.getRawData().getUserId(), "할인명이 비어있습니다.");
            }

            // addon 은 할인에서 제외
            String productType = product.getProductType().toString().toLowerCase();
            if("addon".equals(productType)){
                continue;
            }

            if("mobile".equals(productType)) {
                hasMobile = true;
                mobilePrice = product.getPrice();
            }
            if("internet".equals(productType)){
                hasInternet = true;
                internetPrice = product.getPrice();
            }

            // Mobile_internet 결합 할인 처리(MULTI)
            if(discountPolicy.getCalOrder() == CalOrderType.MULTI){
                if(discountPolicy.getDiscountRange() == DiscountRangeType.MOBLIE_INTERNET){
                    if(!discounts.isEmpty()){   // 큐에 다른 할인 남아있으면 맨 뒤로 보냄
                        discounts.offer(usd);
                        continue;
                    }
                    // 큐가 비어있는 마지막 순서 --> 두 상품 중에 하나라도 있을 때 합산 적용하기
                    if(!hasMobile && !hasInternet){
                        continue;
                    }else {
                        throw BillingException.invalidDiscountPolicyData(discountPolicy.getDiscountPolicyId(), work.getRawData().getUserId());
                    }   // 작업 여기까지 함
                }else{
                    throw BillingException.invalidDiscountPolicyData(discountPolicy.getDiscountPolicyId(), work.getRawData().getUserId());
                }
            }

            else{
                String contentType = discount.getContentType().toString().toLowerCase();
                int configValue = discount.getValue() == null ? 0 : discount.getValue();

                // 그룹 검증
                if("group".equals(contentType)){
                    // 그룹 아이디 확인 --> 인원 수 일치 여부 확인
                    if(work.getRawData().getGroupId() == null || numOfMember != configValue){
                        log.debug("[skip] 그룹 조건 미충족 - User: {}, Member: {}, Required: {}",
                            work.getRawData().getUserId(), numOfMember,contentType);
                            continue;   // 다음 할인으로 넘어감
                    };
                }
                else if("year".equals(contentType)){
                    if(joinedYear < configValue){
                        log.debug("[skip] 가입 년수 미달 - User: {}, Joined: {}, Required: {}",
                                work.getRawData().getUserId(),joinedYear,configValue);
                        continue;   // 다음으로
                    }
                }
            }


            }
            // 할인 금액 계산
            int price = 0;
            if (discount.getIsCash() == DiscountType.CASH && discount.getCash() != null) {
                // 정해진 금액 할인
                if (discount.getCash() <= 0) {
                    throw BillingException.invalidDiscountData(work.getRawData().getUserId(), String.valueOf(usd.getDiscountId()));
                }

                price = discount.getCash();
            } else if (discount.getIsCash() == DiscountType.RATE && discount.getPercent() != null) {
                // 특정 비율 할인
                if (discount.getPercent() <= 0 || discount.getPercent() > 100) {
                    throw BillingException.invalidDiscountData(work.getRawData().getUserId(), String.valueOf(usd.getDiscountId()));
                }
                if (product.getPrice() < 0) {
                    throw BillingException.invalidProductData(work.getRawData().getUserId(), String.valueOf(usd.getDiscountId()));
                }
                price = (int) ((product.getPrice() * discount.getPercent()) / 100);
            } else {
                throw BillingException.invalidDiscountData(work.getRawData().getUserId(), String.valueOf(usd.getDiscountId()));
            }


            totalDiscount += price;

//            log.info("[DiscountCalculationProcessor] 할인 금액 합산 완료");

            // 할인 상세 내역 저장
            work.getDiscounts().add(SettlementDetailsDto.DetailItem.builder()
                    .productType("DISCOUNT")
                    .productName(discount.getDiscountName())
                    .price(price * -1)
                    .build());
        }

        // 총 할인 금액 저장
        if (totalDiscount > Integer.MAX_VALUE) {
            throw BillingFatalException.invalidDiscountAmount(work.getRawData().getUserId(), totalDiscount);
        }

        work.setDiscountAmount((int)totalDiscount);

        // 총 정산 금액 업데이트
        int totalPrice = work.getBaseAmount() - work.getDiscountAmount();
        work.setTotalPrice(Math.max(0, totalPrice));

        return work;
    }
}
