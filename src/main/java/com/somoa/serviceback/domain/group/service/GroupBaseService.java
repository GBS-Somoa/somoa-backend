package com.somoa.serviceback.domain.group.service;

import org.springframework.transaction.annotation.Transactional;

import com.somoa.serviceback.domain.group.entity.Group;
import com.somoa.serviceback.domain.group.entity.GroupUser;
import com.somoa.serviceback.domain.group.error.GroupErrorCode;
import com.somoa.serviceback.domain.group.exception.GroupException;
import com.somoa.serviceback.domain.group.repository.GroupRepository;
import com.somoa.serviceback.domain.group.repository.GroupUserRepository;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@Transactional(readOnly = true)
@AllArgsConstructor
public abstract class GroupBaseService {

	protected final GroupRepository groupRepository;
	protected final GroupUserRepository groupUserRepository;

	protected Mono<Group> findGroup(Integer groupId) {
		return groupRepository.findById(groupId)
			.switchIfEmpty(Mono.error(new GroupException(GroupErrorCode.GROUP_NOT_FOUND)));
	}

	protected Mono<GroupUser> findGroupUser(Integer groupId, Integer userId) {
		return groupUserRepository.findGroupUser(groupId, userId)
			.switchIfEmpty(Mono.error(new GroupException(GroupErrorCode.USER_NOT_IN_GROUP)));
	}

	protected Mono<Boolean> hasGroupManagementPermission(Integer groupId, Integer userId) {
		return groupUserRepository.findGroupManager(groupId)
			.map(groupManager -> groupManager.getUserId().equals(userId))
			.defaultIfEmpty(false);
	}

	protected Mono<Boolean> existsGroupUser(Integer groupId, Integer userId) {
		return groupUserRepository.existsGroupUser(groupId, userId);
	}

	protected Mono<GroupUser> findGroupManager(Integer groupId) {
		return groupUserRepository.findGroupManager(groupId);
	}

	protected Mono<Integer> countJoinGroup(Integer userId) {
		return groupUserRepository.countJoinGroup(userId);
	}
}
