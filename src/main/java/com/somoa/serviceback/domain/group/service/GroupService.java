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
import com.somoa.serviceback.domain.group.repository.GroupRepository;
import com.somoa.serviceback.domain.group.repository.GroupUserRepository;
import com.somoa.serviceback.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupUserRepository groupUserRepository;
    private final UserRepository userRepository;

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
                    return groupRepository.findById(groupId)
                        .flatMap(group -> groupUserRepository.findGroupUser(groupId, userId)
                            .map(groupUser -> GroupDetailResponse.of(group, groupUser)));
                } else {
                    return Mono.error(new IllegalArgumentException("그룹 번호가 유효하지 않거나, 그룹에 속하지 않은 유저입니다."));
                }
            });
    }

    @Transactional
    public Mono<Map<String, Object>> modify(Integer userId, Integer groupId, GroupModifyParam param) {
        return groupRepository.findById(groupId)
            .flatMap(group -> {
                return groupUserRepository.findGroupManager(group.getId())
                    .flatMap(groupManager -> {
                        if (!groupManager.getId().equals(userId)) {
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
    public Mono<Integer> delete(Integer userId, Integer groupId) {
        return groupRepository.findById(groupId)
            .flatMap(group -> {
                return groupUserRepository.findGroupManager(group.getId())
                    .flatMap(groupManager -> {
                        if (!groupManager.getId().equals(userId)) {
                            return Mono.error(new IllegalArgumentException("그룹 삭제 권한이 없는 유저입니다."));
                        } else {
                            return groupRepository.delete(group)
                                .then(Mono.just(group.getId()));
                        }
                    });
            })
            .switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalArgumentException("유효하지 않는 그룹 번호입니다."))));
    }

    @Transactional
    public Mono<Integer> leave(Integer userId, Integer groupId) {
        return groupUserRepository.findGroupUser(groupId, userId)
            .flatMap(existingGroupUser -> {
                    if (existingGroupUser.getRole().equals(GroupUserRole.MANAGER)) {
                        return Mono.error(new RuntimeException("관리자는 그룹에서 나갈 수 없습니다."));
                    } else {
                        return groupUserRepository.delete(existingGroupUser)
                                .then(Mono.just(existingGroupUser.getId()));
                    }
                })
            .switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalArgumentException("그룹에 속하지 않은 유저입니다."))));
    }

    public Mono<List<GroupUserResponse>> getMembers(Integer userId, Integer groupId) {
        return groupUserRepository.existsGroupUser(groupId, userId)
            .flatMap(userExists -> {
                if (userExists) {
                    return groupUserRepository.findAllByGroupId(groupId)
                        .collectList();
                } else {
                    return Mono.error(new IllegalArgumentException("그룹 번호가 유효하지 않거나, 그룹에 속하지 않은 유저입니다."));
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
                                return Mono.error(new IllegalArgumentException("이미 그룹에 등록된 유저입니다."));
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
                    .switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalArgumentException("유효하지 않는 그룹 번호입니다.")))))
            .switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalArgumentException("존재하지 않는 유저입니다."))));
    }

    @Transactional
    public Mono<Map<String, Object>> modifyMemberPermission(Integer userId, Integer groupId, Integer targetUserId, String role) {
        if (!GroupUserRole.isValidRole(role))
            return Mono.error(new IllegalArgumentException("유효하지 않는 그룹 유저 권한입니다."));
        
        return groupRepository.findById(groupId)
            .flatMap(group -> groupUserRepository.findGroupManager(group.getId())
                .flatMap(groupManager -> {
                    if (!groupManager.getId().equals(userId)) {
                        return Mono.error(new IllegalArgumentException("그룹 멤버 수정 권한이 없는 유저입니다."));
                    }

                    return groupUserRepository.findGroupUser(group.getId(), targetUserId)
                        .flatMap(targetUser -> {
                            targetUser.setRole(role);
                            return groupUserRepository.save(targetUser)
                                .map(updatedUser -> {
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("groupId", updatedUser.getGroupId());
                                    data.put("userId", updatedUser.getUserId());
                                    data.put("role", updatedUser.getRole());
                                    return data;
                                });
                        })
                        .switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalArgumentException("존재하지 않는 유저입니다."))));
                }))
            .switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalArgumentException("유효하지 않는 그룹 번호입니다."))));
    }
}
