package com.somoa.serviceback.domain.group.service;

import com.somoa.serviceback.domain.group.error.GroupErrorCode;
import com.somoa.serviceback.domain.group.exception.GroupException;
import com.somoa.serviceback.domain.group.repository.GroupRepository;
import com.somoa.serviceback.domain.group.repository.GroupUserRepository;
import com.somoa.serviceback.domain.order.dto.OrderResponse;
import com.somoa.serviceback.domain.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GroupOrderService extends GroupBaseService{

    private final OrderRepository orderRepository;

    public GroupOrderService(GroupRepository groupRepository,
                             GroupUserRepository groupUserRepository,
                             OrderRepository orderRepository) {
        super(groupRepository, groupUserRepository);
        this.orderRepository = orderRepository;
    }

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
