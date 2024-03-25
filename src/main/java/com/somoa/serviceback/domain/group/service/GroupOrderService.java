package com.somoa.serviceback.domain.group.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.somoa.serviceback.domain.group.error.GroupErrorCode;
import com.somoa.serviceback.domain.group.exception.GroupException;
import com.somoa.serviceback.domain.group.repository.GroupUserRepository;
import com.somoa.serviceback.domain.order.dto.OrderResponse;
import com.somoa.serviceback.domain.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupOrderService {

    private final GroupUserRepository groupUserRepository;
    private final OrderRepository orderRepository;

    public Mono<List<OrderResponse>> getOrders(Integer userId, Integer groupId) {
        return groupUserRepository.existsGroupUser(groupId, userId)
            .flatMap(userExists -> {
                if (userExists) {
                    return orderRepository.findAllByGroupId(groupId)
                        .map(OrderResponse::of)
                        .collectList();
                } else {
                    return Mono.error(new GroupException(GroupErrorCode.INVALID_GROUP_OR_USER));
                }
            });
    }
}
