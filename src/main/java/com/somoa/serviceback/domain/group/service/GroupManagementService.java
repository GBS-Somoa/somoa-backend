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
        return findGroup(groupId)
            .flatMap(group -> findGroupUser(groupId, userId)
                .map(groupUser -> GroupDetailResponse.of(group, groupUser)));
    }

    @Transactional
    public Mono<Void> modify(Integer userId, Integer groupId, GroupModifyParam param) {
        return hasGroupManagementPermission(groupId, userId)
            .flatMap(hasPermission -> {
                if (!hasPermission) {
                    return Mono.error(new GroupException(GroupErrorCode.NO_GROUP_MANAGEMENT_PERMISSION));
                }
                return findGroup(groupId)
                    .flatMap(group -> {
                        group.setName(param.getGroupName());
                        return groupRepository.save(group);
                    }).then();
            });
    }

    @Transactional
    public Mono<Void> delete(Integer userId, Integer groupId) {
        return hasGroupManagementPermission(groupId, userId)
            .flatMap(hasPermission -> {
                if (!hasPermission) {
                    return Mono.error(new GroupException(GroupErrorCode.NO_GROUP_MANAGEMENT_PERMISSION));
                }
                return findGroup(groupId)
                    .flatMap(groupRepository::delete);
            });
    }

    private Mono<Group> findGroup(Integer groupId) {
        return groupRepository.findById(groupId)
            .switchIfEmpty(Mono.error(new GroupException(GroupErrorCode.GROUP_NOT_FOUND)));
    }

    private Mono<GroupUser> findGroupUser(Integer groupId, Integer userId) {
        return groupUserRepository.findGroupUser(groupId, userId)
            .switchIfEmpty(Mono.error(new GroupException(GroupErrorCode.USER_NOT_IN_GROUP)));
    }

    private Mono<Boolean> hasGroupManagementPermission(Integer groupId, Integer userId) {
        return groupUserRepository.findGroupManager(groupId)
            .map(groupManager -> groupManager.getUserId().equals(userId))
            .defaultIfEmpty(false);
    }
}
