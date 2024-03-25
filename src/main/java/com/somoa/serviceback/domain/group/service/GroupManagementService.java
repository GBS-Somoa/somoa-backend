package com.somoa.serviceback.domain.group.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.somoa.serviceback.domain.group.dto.GroupDetailResponse;
import com.somoa.serviceback.domain.group.dto.GroupModifyParam;
import com.somoa.serviceback.domain.group.dto.GroupRegisterParam;
import com.somoa.serviceback.domain.group.dto.GroupResponse;
import com.somoa.serviceback.domain.group.entity.Group;
import com.somoa.serviceback.domain.group.entity.GroupUser;
import com.somoa.serviceback.domain.group.entity.GroupUserRole;
import com.somoa.serviceback.domain.group.error.GroupErrorCode;
import com.somoa.serviceback.domain.group.exception.GroupException;
import com.somoa.serviceback.domain.group.repository.GroupRepository;
import com.somoa.serviceback.domain.group.repository.GroupUserRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupManagementService {

    private final GroupRepository groupRepository;
    private final GroupUserRepository groupUserRepository;

    @Transactional
    public Mono<Map<String, Object>> save(Integer userId, GroupRegisterParam param) {
        return groupRepository.save(Group.builder()
                        .name(param.getGroupName())
                        .build())
                .flatMap(savedGroup -> {
                    GroupUser groupUser = GroupUser.builder()
                            .groupId(savedGroup.getId())
                            .userId(userId)
                            .role(GroupUserRole.MANAGER)
                            .orderedNum(0)  // TODO: orderNum 맨 마지막으로 할당해야 함
                            .alarm(true)
                            .build();
                    return groupUserRepository.save(groupUser);
                })
                .map(groupUser -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("groupId", groupUser.getGroupId());
                    return data;
                });
    }

    public Flux<GroupResponse> findAll(Integer userId) {
        return groupRepository.findAllByUserId(userId)
            .map(GroupResponse::of);
    }

    public Mono<GroupDetailResponse> findOne(Integer userId, Integer groupId) {
        return groupRepository.findById(groupId)
            .flatMap(group -> groupUserRepository.findGroupUser(group.getId(), userId)
                .map(groupUser -> GroupDetailResponse.of(group, groupUser))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new GroupException(GroupErrorCode.USER_NOT_IN_GROUP)))))
            .switchIfEmpty(Mono.defer(() -> Mono.error(new GroupException(GroupErrorCode.GROUP_NOT_FOUND))));
    }

    @Transactional
    public Mono<Integer> modify(Integer userId, Integer groupId, GroupModifyParam param) {
        return groupRepository.findById(groupId)
            .flatMap(group -> {
                return groupUserRepository.findGroupManager(group.getId())
                    .flatMap(groupManager -> {
                        if (!groupManager.getId().equals(userId)) {
                            return Mono.error(new GroupException(GroupErrorCode.NO_GROUP_MANAGEMENT_PERMISSION));
                        } else {
                            group.setName(param.getGroupName());
                            return groupRepository.save(group)
                                .then(Mono.just(group.getId()));
                        }
                    });
            })
            .switchIfEmpty(Mono.defer(() -> Mono.error(new GroupException(GroupErrorCode.GROUP_NOT_FOUND))));
    }

    @Transactional
    public Mono<Integer> delete(Integer userId, Integer groupId) {
        return groupRepository.findById(groupId)
            .flatMap(group -> {
                return groupUserRepository.findGroupManager(group.getId())
                    .flatMap(groupManager -> {
                        if (!groupManager.getId().equals(userId)) {
                            return Mono.error(new GroupException(GroupErrorCode.NO_GROUP_MANAGEMENT_PERMISSION));
                        } else {
                            return groupRepository.delete(group)
                                .then(Mono.just(group.getId()));
                        }
                    });
            })
            .switchIfEmpty(Mono.defer(() -> Mono.error(new GroupException(GroupErrorCode.GROUP_NOT_FOUND))));
    }
}
