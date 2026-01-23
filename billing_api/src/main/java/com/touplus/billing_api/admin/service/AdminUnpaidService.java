package com.touplus.billing_api.admin.service;

import com.touplus.billing_api.admin.dto.UnpaidUserResponse;

import java.util.List;

public interface AdminUnpaidService {

    List<UnpaidUserResponse> getUnpaidUsers();
}
