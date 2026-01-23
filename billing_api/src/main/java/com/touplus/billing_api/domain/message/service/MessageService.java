package com.touplus.billing_api.domain.message.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.touplus.billing_api.admin.dto.MessageWithSettlementMonthDto;
import com.touplus.billing_api.admin.dto.PageResponse;
import com.touplus.billing_api.domain.message.enums.MessageStatus;

@Service
public interface MessageService {
	
	List<MessageWithSettlementMonthDto> getAllMessages();

	List<MessageWithSettlementMonthDto> getAllMessages(int page);

    List<MessageWithSettlementMonthDto> getMessagesBySettlementMonth(String settlementMonth, int page);

    PageResponse<MessageWithSettlementMonthDto> getMessagesByStatus(MessageStatus messageStatus, int page);

    PageResponse<MessageWithSettlementMonthDto> getMessagesWithPagination(MessageStatus messageStatus, String settlementMonth, int page);
}