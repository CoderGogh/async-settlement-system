package com.touplus.billing_batch.jobs.billing.step.reader;

import com.touplus.billing_batch.domain.dto.*;
import com.touplus.billing_batch.domain.entity.BillingUser;
import com.touplus.billing_batch.domain.repository.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// targetMonth 를 어디서 활용...?
@Component
@StepScope
@Slf4j
public class BillingItemReader implements ItemReader<BillingUserBillingInfoDto> {

    private final BillingUserRepository userRepository;
    private final UserSubscribeProductRepository uspRepository;
    private final AdditionalChargeRepository chargeRepository;
    private final UserSubscribeDiscountRepository discountRepository;

    public BillingItemReader(
            BillingUserRepository userRepository,
            UserSubscribeProductRepository uspRepository,
            AdditionalChargeRepository chargeRepository,
            UserSubscribeDiscountRepository discountRepository
    ) {
        this.userRepository = userRepository;
        this.uspRepository = uspRepository;
        this.chargeRepository = chargeRepository;
        this.discountRepository = discountRepository;
    }

    @Value("#{stepExecutionContext['minValue']}")
    private Long minValue;

    @Value("#{stepExecutionContext['maxValue']}")
    private Long maxValue;

    @Value("#{jobParameters['startDate']}")
    private LocalDate startDate;

    @Value("#{jobParameters['endDate']}")
    private LocalDate endDate;

    @Value("#{jobParameters['forceFullScan'] ?: false}")
    private boolean forceFullScan;

    @Value("#{jobParameters['chunkSize'] ?: 2000}")
    private int chunkSize;

    @Value("#{jobParameters['targetMonth']}")
    private String targetMonth;

    // groupId와 usage를 제공해야 함!

    private Long lastProcessedUserId = 0L;
    private List<BillingUserBillingInfoDto> buffer = new ArrayList<>();
    private int nextIndex = 0;

    @Override
    public BillingUserBillingInfoDto read() {
        // 청크 단위로 데이터 읽기 --> 베퍼에 저장 --> 하나씩 pop
        if (nextIndex >= buffer.size()) {
            fillBuffer();   // DB에서 fill
            nextIndex = 0;
            if (buffer.isEmpty()) return null;  // 배치 종료
        }
        return buffer.get(nextIndex++);
    }

    private void fillBuffer() {
        buffer.clear();

        // 1. 청구 대상 유저 선택 & groupMember 수 파악
        // '미정구 유저 찾기'
        List<BillingUser> users = userRepository.findUsersInRange(
                minValue, maxValue, lastProcessedUserId, forceFullScan, startDate, endDate, Pageable.ofSize(chunkSize)
        );

        if (users.isEmpty()) return;

        // 2. Entity를 조회 후 DTO로 변환하여 Map으로 그룹화(Bulk 조회 준비동작)
        List<Long> userIds = users.stream().map(BillingUser::getUserId).toList();

        // 2. Entity를 조회 후 DTO로 변환하여 Map으로 그룹화 (Bulk 조회 준비동작)

        // 유저 별 구독중인 상품 정보 수집
        Map<Long, List<UserSubscribeProductDto>> uspMap = uspRepository.findByUserIdIn(userIds, startDate, endDate)
                .stream()
                .map(UserSubscribeProductDto::fromEntity)
                .collect(Collectors.groupingBy(UserSubscribeProductDto::getUserId));

        // 유저 별 추가요금 정보 수집
        Map<Long, List<AdditionalChargeDto>> chargeMap = chargeRepository.findByUserIdIn(userIds, startDate, endDate)
                .stream()
                .map(AdditionalChargeDto::fromEntity)
                .collect(Collectors.groupingBy(AdditionalChargeDto::getUserId));

        // 유저 별 할인 정보 수집
        Map<Long, List<UserSubscribeDiscountDto>> discountMap = discountRepository.findByUserIdIn(userIds, startDate, endDate)
                .stream()
                .map(UserSubscribeDiscountDto::fromEntity)
                .collect(Collectors.groupingBy(UserSubscribeDiscountDto::getUserId));


        // 3. DTO 조립 --> processor 로 넘길 정보
        for (BillingUser user : users) {
            BillingUserBillingInfoDto dto = BillingUserBillingInfoDto.builder()
                    .userId(user.getUserId())
                    .products(uspMap.getOrDefault(user.getUserId(), List.of()))
                    .additionalCharges(chargeMap.getOrDefault(user.getUserId(), List.of()))
                    .discounts(discountMap.getOrDefault(user.getUserId(), List.of()))
                    .numOfMember(user.getNumOfMember())
                    .build();

            buffer.add(dto);
            lastProcessedUserId = user.getUserId();
        }
        log.info("Buffer filled with {} records. LastProcessedUserId: {}", buffer.size(), lastProcessedUserId);
    }
}