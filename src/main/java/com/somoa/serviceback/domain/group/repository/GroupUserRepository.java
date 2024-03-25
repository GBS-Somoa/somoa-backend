package com.somoa.serviceback.domain.group.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.somoa.serviceback.domain.group.dto.GroupUserResponse;
import com.somoa.serviceback.domain.group.entity.GroupUser;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GroupUserRepository extends ReactiveCrudRepository<GroupUser, Integer> {

	@Query("SELECT EXISTS (SELECT 1 "
		+ "					FROM group_user "
		+ "					WHERE group_id = :groupId AND user_id = :userId)")
	Mono<Boolean> existsGroupUser(Integer groupId, Integer userId);

	@Query("SELECT * "
		+ "	  FROM group_user "
		+ "	 WHERE group_id = :groupId AND user_id = :userId")
	Mono<GroupUser> findGroupUser(Integer groupId, Integer userId);

	@Query("SELECT * "
		+ "   FROM group_user "
		+ "  WHERE group_id = :groupId AND role = '관리자'")
	Mono<GroupUser> findGroupManager(Integer groupId);

	@Query("SELECT u.user_id, u.user_nickname, gu.role "
		+ "   FROM group_user gu "
		+ "   JOIN user u "
		+ "	    ON gu.user_id = u.user_id "
		+ "  WHERE gu.group_id = :groupId")
	Flux<GroupUserResponse> findAllByGroupId(Integer groupId);
}