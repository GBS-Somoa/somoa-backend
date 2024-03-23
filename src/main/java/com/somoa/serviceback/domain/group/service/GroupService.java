package com.somoa.serviceback.domain.group.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.somoa.serviceback.domain.group.dto.GroupDetailResponse;
import com.somoa.serviceback.domain.group.dto.GroupModifyParam;
import com.somoa.serviceback.domain.group.dto.GroupRegisterParam;
import com.somoa.serviceback.domain.group.dto.GroupResponse;
import com.somoa.serviceback.domain.group.dto.GroupUserRegisterParam;
import com.somoa.serviceback.domain.group.entity.Group;
import com.somoa.serviceback.domain.group.entity.GroupUser;
import com.somoa.serviceback.domain.group.entity.GroupUserRole;
import com.somoa.serviceback.domain.group.repository.GroupRepository;
import com.somoa.serviceback.domain.group.repository.GroupUserRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GroupService {

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
                    data.put("groupUserId", groupUser.getId());
                    return data;
                });
    }

    public Flux<GroupResponse> findAll(Integer userId) {
        return groupRepository.findAllByUserId(userId)
            .map(GroupResponse::of);
    }

    public Mono<GroupDetailResponse> findOne(Integer userId, Integer groupId) {
        return groupUserRepository.existsGroupUser(groupId, userId)
            .flatMap(userExists -> {
                if (userExists) {
                    return groupRepository.findByIdWithUserInfo(groupId, userId)
                        .flatMap(group -> groupUserRepository.findAllSimple(groupId)
                            .collectList()
                            .map(groupMembers -> GroupDetailResponse.of(group, groupMembers)));
                } else {
                    return Mono.error(new IllegalArgumentException("그룹에 대한 권한이 없는 유저입니다."));
                }
            });
    }

    @Transactional
    public Mono<Map<String, Object>> modify(Integer userId, Integer groupId, GroupModifyParam param) {
        return groupRepository.findById(groupId)
            .flatMap(group -> {
                return groupUserRepository.findGroupManager(group.getId())
                    .flatMap(managerId -> {
                        if (!managerId.equals(userId)) {
                            return Mono.error(new IllegalArgumentException("그룹 수정 권한이 없는 유저입니다."));
                        } else {
                            group.setName(param.getGroupName());
                            return groupRepository.save(group)
                                .map(modifiedGroup -> {
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("groupId", group.getId());
                                    data.put("groupName", group.getName());
                                    return data;
                                });
                        }
                    });
            })
            .switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalArgumentException("유효하지 않는 그룹 번호입니다."))));
    }

    @Transactional
    public Mono<?> delete(Integer userId, Integer groupId) {
        return groupRepository.findById(groupId)
            .flatMap(group -> {
                return groupUserRepository.findGroupManager(group.getId())
                    .flatMap(managerId -> {
                        if (!managerId.equals(userId)) {
                            return Mono.error(new IllegalArgumentException("그룹 삭제 권한이 없는 유저입니다."));
                        } else {
                            return groupRepository.delete(group)
                                .then(Mono.fromCallable(() -> {
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("groupId", groupId);
                                    return data;
                                }));
                        }
                    });
            })
            .switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalArgumentException("유효하지 않는 그룹 번호입니다."))));
    }

    @Transactional
    public Mono<Map<String, Object>> addMember(Integer groupId, GroupUserRegisterParam param) {
        final Integer userId = param.getUserId();

        // response data
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        data.put("userId", userId);

        return groupUserRepository.existsGroupUser(groupId, userId)
            .flatMap(userExists -> {
                if (userExists) {
                    return Mono.error(new IllegalArgumentException("이미 그룹에 등록된 유저입니다."));
                } else {
                    return Mono.defer(() -> {
                            GroupUser groupUser = GroupUser.builder()
                                .groupId(groupId)
                                .userId(userId)
                                .role(GroupUserRole.USER_ALL)
                                .orderedNum(0)  // TODO: orderNum 맨 마지막으로 할당해야 함
                                .alarm(true)
                                .build();
                            return groupUserRepository.save(groupUser)
                                .then(Mono.just(data));
                        }
                    );
                }
            });
    }
}
