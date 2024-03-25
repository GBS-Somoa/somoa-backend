package com.somoa.serviceback.domain.group.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.somoa.serviceback.domain.group.dto.GroupDetailResponse;
import com.somoa.serviceback.domain.group.dto.GroupModifyParam;
import com.somoa.serviceback.domain.group.dto.GroupRegisterParam;
import com.somoa.serviceback.domain.group.dto.GroupResponse;
import com.somoa.serviceback.domain.group.dto.GroupUserRegisterParam;
import com.somoa.serviceback.domain.group.dto.GroupUserResponse;
import com.somoa.serviceback.domain.group.entity.Group;
import com.somoa.serviceback.domain.group.entity.GroupUser;
import com.somoa.serviceback.domain.group.entity.GroupUserRole;
import com.somoa.serviceback.domain.group.error.GroupErrorCode;
import com.somoa.serviceback.domain.group.exception.GroupException;
import com.somoa.serviceback.domain.group.repository.GroupRepository;
import com.somoa.serviceback.domain.group.repository.GroupUserRepository;
import com.somoa.serviceback.domain.order.dto.OrderResponse;
import com.somoa.serviceback.domain.order.repository.OrderRepository;
import com.somoa.serviceback.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupUserService {

    private final GroupRepository groupRepository;
    private final GroupUserRepository groupUserRepository;
    private final UserRepository userRepository;

    public Mono<List<GroupUserResponse>> getMembers(Integer userId, Integer groupId) {
        return groupUserRepository.existsGroupUser(groupId, userId)
            .flatMap(userExists -> {
                if (userExists) {
                    return groupUserRepository.findAllByGroupId(groupId)
                        .collectList();
                } else {
                    return Mono.error(new GroupException(GroupErrorCode.INVALID_GROUP_OR_USER));
                }
            });
    }

    @Transactional
    public Mono<Integer> addMember(Integer groupId, GroupUserRegisterParam param) {
        final Integer userId = param.getUserId();

        return userRepository.findById(userId)
                .flatMap(user -> groupRepository.findById(groupId)
                    .flatMap(group -> groupUserRepository.existsGroupUser(group.getId(), userId)
                        .flatMap(userExists -> {
                            if (userExists) {
                                return Mono.error(new GroupException(GroupErrorCode.USER_ALREADY_IN_GROUP));
                            } else {
                                return Mono.defer(() -> groupUserRepository.save(GroupUser.builder()
                                        .groupId(groupId)
                                        .userId(userId)
                                        .role(GroupUserRole.USER_ALL)
                                        .orderedNum(0)  // TODO: orderNum 맨 마지막으로 할당해야 함
                                        .alarm(true)
                                        .build())
                                    .map(GroupUser::getId));
                            }
                        }))
                    .switchIfEmpty(Mono.defer(() -> Mono.error(new GroupException(GroupErrorCode.GROUP_NOT_FOUND)))))
            .switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalArgumentException("존재하지 않는 유저입니다."))));
    }

    @Transactional
    public Mono<Integer> modifyMemberPermission(Integer userId, Integer groupId, Integer targetUserId, String role) {
        if (!GroupUserRole.isValidRole(role))
            return Mono.error(new GroupException(GroupErrorCode.INVALID_GROUP_USER_ROLE));
        
        return groupRepository.findById(groupId)
            .flatMap(group -> groupUserRepository.findGroupManager(group.getId())
                .flatMap(groupManager -> {
                    if (!groupManager.getId().equals(userId)) {
                        return Mono.error(new GroupException(GroupErrorCode.NO_GROUP_MANAGEMENT_PERMISSION));
                    }

                    return groupUserRepository.findGroupUser(group.getId(), targetUserId)
                        .flatMap(targetUser -> {
                            targetUser.setRole(role);
                            return groupUserRepository.save(targetUser)
                                .then(Mono.just(targetUser.getId()));
                        })
                        .switchIfEmpty(Mono.defer(() -> Mono.error(new GroupException(GroupErrorCode.USER_NOT_IN_GROUP))));
                }))
            .switchIfEmpty(Mono.defer(() -> Mono.error(new GroupException(GroupErrorCode.GROUP_NOT_FOUND))));
    }

    @Transactional
    public Mono<Integer> leave(Integer userId, Integer groupId) {
        return groupUserRepository.findGroupUser(groupId, userId)
            .flatMap(existingGroupUser -> {
                if (existingGroupUser.getRole().equals(GroupUserRole.MANAGER)) {
                    return Mono.error(new GroupException(GroupErrorCode.GROUP_MANAGER_CANNOT_LEAVE));
                } else {
                    return groupUserRepository.delete(existingGroupUser)
                        .then(Mono.just(existingGroupUser.getId()));
                }
            })
            .switchIfEmpty(Mono.defer(() -> Mono.error(new GroupException(GroupErrorCode.USER_NOT_IN_GROUP))));
    }
}
