package com.touplus.billing_api.domain.message.service.impl;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.touplus.billing_api.domain.message.dto.MessageStatusSummaryDto;
import com.touplus.billing_api.domain.message.service.MessageDashBoardService;
import com.touplus.billing_api.domain.repository.message.MessageDashBoardRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageDashBoardServiceImpl implements MessageDashBoardService {

    private final MessageDashBoardRepository messageDashBoardRepository;

    @Override
    public MessageStatusSummaryDto getMessageStatusSummary() {

        LocalDate lastMonthDate = getLastMonthDate();

        long total = messageDashBoardRepository.countBySettlementMonth(lastMonthDate);
        long sent = messageDashBoardRepository.countBySettlementMonthAndStatus(lastMonthDate, "SENT");
        long fail = messageDashBoardRepository.countBySettlementMonthAndStatus(lastMonthDate, "FAILED");

        double sentRate = total == 0 ? 0 : (sent * 100.0 / total);
        double failRate = total == 0 ? 0 : (fail * 100.0 / total);

        // yyyy-MM 포맷으로 변환
        String settlementMonthStr = lastMonthDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        return MessageStatusSummaryDto.builder()
                .totalCount(total)
                .sentCount(sent)
                .failCount(fail)
                .sentRate(sentRate)
                .failRate(failRate)
                .settlementMonth(settlementMonthStr)
                .build();
    }

    private LocalDate getLastMonthDate() {
        return LocalDate.now()
                .minusMonths(1)
                .withDayOfMonth(1);
    }
}