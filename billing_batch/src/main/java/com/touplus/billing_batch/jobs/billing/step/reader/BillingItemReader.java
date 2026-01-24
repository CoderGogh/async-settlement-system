package com.touplus.billing_batch.jobs.billing.step.reader;

import com.touplus.billing_batch.domain.dto.*;
import com.touplus.billing_batch.domain.entity.BillingUser;
import com.touplus.billing_batch.domain.repository.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class BillingItemReader implements ItemReader<BillingUserBillingInfoDto> {

    private final BillingUserRepository userRepository;
    private final UserSubscribeProductRepository uspRepository;
    private final AdditionalChargeRepository chargeRepository;
    private final UserSubscribeDiscountRepository discountRepository;

    private final Long minValue;
    private final Long maxValue;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final boolean forceFullScan;
    private final int chunkSize;

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
                    // Null 방지 --> List.of() 사용
                    .products(uspMap.getOrDefault(user.getUserId(), List.of()))
                    .additionalCharges(chargeMap.getOrDefault(user.getUserId(), List.of()))
                    .discounts(discountMap.getOrDefault(user.getUserId(), List.of()))
                    .numOfMember(user.getNumOfMember()) // 그룹에 속한 인원수
                    .build();

            buffer.add(dto);
            lastProcessedUserId = user.getUserId();
        }
        log.info("Buffer filled with {} records. LastProcessedUserId: {}", buffer.size(), lastProcessedUserId);
    }
}