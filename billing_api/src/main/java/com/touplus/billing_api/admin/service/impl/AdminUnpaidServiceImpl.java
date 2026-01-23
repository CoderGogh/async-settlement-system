package com.touplus.billing_api.admin.service.impl;

import com.touplus.billing_api.admin.dto.UnpaidUserResponse;
import com.touplus.billing_api.admin.service.AdminUnpaidService;
import com.touplus.billing_api.domain.billing.entity.Unpaid;
import com.touplus.billing_api.domain.message.entity.User;
import com.touplus.billing_api.domain.message.service.UserContactService;
import com.touplus.billing_api.domain.repository.billing.UnpaidRepository;
import com.touplus.billing_api.domain.repository.message.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUnpaidServiceImpl implements AdminUnpaidService {

    private final UnpaidRepository unpaidRepository;
    private final UserRepository userRepository;
    private final UserContactService userContactService;

    @Override
    public List<UnpaidUserResponse> getUnpaidUsers() {

        List<Unpaid> unpaids = unpaidRepository.findUnpaidUsers();
        if (unpaids.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = unpaids.stream()
                .map(Unpaid::getUserId)
                .distinct()
                .toList();

        Map<Long, User> userMap = userRepository.findByIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        return unpaids.stream()
                .map(unpaid -> {
                    User user = userMap.get(unpaid.getUserId());

                    return new UnpaidUserResponse(
                            unpaid.getId(),
                            unpaid.getUnpaidPrice(),
                            unpaid.getUnpaidMonth(),
                            userContactService.decryptAndMask(user)
                    );
                })
                .toList();
    }
}