package com.somoa.serviceback.domain.group.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.somoa.serviceback.domain.group.dto.GroupUserRegisterParam;
import com.somoa.serviceback.domain.group.dto.GroupUserResponse;
import com.somoa.serviceback.domain.group.entity.GroupUser;
import com.somoa.serviceback.domain.group.entity.GroupUserRole;
import com.somoa.serviceback.domain.group.error.GroupErrorCode;
import com.somoa.serviceback.domain.group.exception.GroupException;
import com.somoa.serviceback.domain.group.repository.GroupRepository;
import com.somoa.serviceback.domain.group.repository.GroupUserRepository;
import com.somoa.serviceback.domain.user.repository.UserRepository;

import reactor.core.publisher.Mono;

@Service
@Transactional(readOnly = true)
public class GroupUserService extends GroupBaseService {

    private final UserRepository userRepository;

    public GroupUserService(GroupRepository groupRepository,
                            GroupUserRepository groupUserRepository,
                            UserRepository userRepository) {
        super(groupRepository, groupUserRepository);
        this.userRepository = userRepository;
    }

    public Mono<List<GroupUserResponse>> getMembers(Integer userId, Integer groupId) {
        return existsGroupUser(groupId, userId)
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
            .flatMap(user -> findGroup(groupId)
                .flatMap(group -> existsGroupUser(groupId, userId)
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
                    })))
            .switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalArgumentException("존재하지 않는 유저입니다."))));
    }

    @Transactional
    public Mono<Void> modifyMemberPermission(Integer userId, Integer groupId, Integer targetUserId, String role) {
        if (!GroupUserRole.isValidRole(role))
            return Mono.error(new GroupException(GroupErrorCode.INVALID_GROUP_USER_ROLE));

        return findGroup(groupId)
            .flatMap(group -> findGroupManager(groupId)
                .flatMap(groupManager -> {
                    if (!groupManager.getId().equals(userId)) {
                        return Mono.error(new GroupException(GroupErrorCode.NO_GROUP_MANAGEMENT_PERMISSION));
                    }
                    return findGroupUser(groupId, targetUserId)
                        .flatMap(targetUser -> {
                            targetUser.setRole(role);
                            return groupUserRepository.save(targetUser)
                                .then();
                        });
                })
            );
    }

    @Transactional
    public Mono<Void> leave(Integer userId, Integer groupId) {
        return findGroupUser(groupId, userId)
            .flatMap(existingGroupUser -> {
                if (existingGroupUser.getRole().equals(GroupUserRole.MANAGER)) {
                    return Mono.error(new GroupException(GroupErrorCode.GROUP_MANAGER_CANNOT_LEAVE));
                } else {
                    return groupUserRepository.delete(existingGroupUser)
                        .then();
                }
            });
    }
}
