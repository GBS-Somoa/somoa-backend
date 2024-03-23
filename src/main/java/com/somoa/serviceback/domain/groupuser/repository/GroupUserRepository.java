package com.somoa.serviceback.domain.groupuser.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.somoa.serviceback.domain.groupuser.entity.GroupUser;
import com.somoa.serviceback.domain.user.dto.UserSimpleResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GroupUserRepository extends ReactiveCrudRepository<GroupUser, Integer> {

	@Query("SELECT EXISTS (SELECT 1 "
		+ "					FROM group_user "
		+ "					WHERE group_id = :groupId AND user_id = :userId)")
	Mono<Boolean> existsGroupUser(Integer groupId, Integer userId);

	@Query("SELECT u.user_id, u.user_nickname, gu.role"
		+ "	  FROM group_user gu"
		+ "	  JOIN user u"
		+ "	    ON gu.user_id = u.user_id"
		+ "  WHERE gu.group_id = :groupId")
	Flux<UserSimpleResponse> findAllSimple(Integer groupId);

	@Query("SELECT gu.user_id"
		+ "   FROM group_user gu"
		+ "  WHERE gu.group_id = :groupId AND gu.role = '관리자'")
	Mono<Integer> findGroupManager(Integer groupId);
}
