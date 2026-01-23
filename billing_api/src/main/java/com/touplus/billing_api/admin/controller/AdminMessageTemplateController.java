package com.touplus.billing_api.admin.controller;


import com.touplus.billing_api.admin.dto.MessageTemplateRequest;
import com.touplus.billing_api.admin.service.MessageTemplateService;
import com.touplus.billing_api.domain.message.entity.MessageTemplate;
import com.touplus.billing_api.domain.message.enums.MessageType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/message-templates")
public class AdminMessageTemplateController {

    private final MessageTemplateService messageTemplateService;

    /**
     * 템플릿 생성
     */
    @PostMapping
    public Long createTemplate(
            @RequestBody @Valid MessageTemplateRequest request
    ) {
        return messageTemplateService.createTemplate(
                request.getTemplateName(),
                request.getMessageType(),
                request.getTemplateContent()
        );
    }

    /**
     * 템플릿 단건 조회
     */
    @GetMapping("/{templateId}")
    public MessageTemplate getTemplate(
            @PathVariable Long templateId
    ) {
        return messageTemplateService.getTemplate(templateId);
    }

    /**
     * 템플릿 전체 조회
     */
    @GetMapping
    public List<MessageTemplate> getTemplates(
            @RequestParam(required = false) MessageType messageType
    ) {
        if (messageType == null) {
            return messageTemplateService.getTemplates();
        }
        return messageTemplateService.getTemplatesByType(messageType);
    }

    /**
     * 템플릿 수정
     */
    @PutMapping("/{templateId}")
    public void updateTemplate(
            @PathVariable Long templateId,
            @RequestBody @Valid MessageTemplateRequest request
    ) {
        messageTemplateService.updateTemplate(
                templateId,
                request.getTemplateName(),
                request.getMessageType(),
                request.getTemplateContent()
        );
    }

    /**
     * 템플릿 삭제 (soft delete)
     */
    @DeleteMapping("/{templateId}")
    public void deleteTemplate(
            @PathVariable Long templateId
    ) {
        messageTemplateService.deleteTemplate(templateId);
    }
}
