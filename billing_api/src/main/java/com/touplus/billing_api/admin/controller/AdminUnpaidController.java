package com.touplus.billing_api.admin.controller;

import com.touplus.billing_api.admin.dto.UnpaidUserResponse;
import com.touplus.billing_api.admin.service.impl.AdminUnpaidServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/unpaid")
@RequiredArgsConstructor
public class AdminUnpaidController {

    private final AdminUnpaidServiceImpl adminUnpaidService;

    @GetMapping
    public List<UnpaidUserResponse> getUnpaidUsers() {
        return adminUnpaidService.getUnpaidUsers();
    }
}
