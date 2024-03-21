package com.somoa.serviceback.domain.group.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.somoa.serviceback.domain.group.dto.GroupRegisterParam;
import com.somoa.serviceback.domain.group.entity.Group;
import com.somoa.serviceback.domain.group.repository.GroupRepository;
import com.somoa.serviceback.domain.groupuser.entity.GroupUser;
import com.somoa.serviceback.domain.groupuser.entity.GroupUserRole;
import com.somoa.serviceback.domain.groupuser.repository.GroupUserRepository;

import lombok.RequiredArgsConstructor;
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
}
