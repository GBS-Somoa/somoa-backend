package com.somoa.serviceback.domain.group.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class GroupManagementService extends GroupBaseService {

    public GroupManagementService(GroupRepository groupRepository,
                                  GroupUserRepository groupUserRepository) {
        super(groupRepository, groupUserRepository);
    }

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

    public Mono<List<GroupResponse>> findAll(Integer userId) {
        return groupRepository.findAllByUserId(userId)
            .collectList();
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
}
