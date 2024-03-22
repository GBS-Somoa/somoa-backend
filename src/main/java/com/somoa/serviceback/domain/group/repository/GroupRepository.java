package com.somoa.serviceback.domain.group.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.somoa.serviceback.domain.group.entity.Group;
import com.somoa.serviceback.domain.group.repository.dto.GroupWithUserInfo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GroupRepository extends ReactiveCrudRepository<Group, Integer> {

	@Query("SELECT * "
		+ "   FROM groups g "
		+ "   JOIN group_user gu "
		+ "     ON g.group_id = gu.group_id "
		+ "  WHERE gu.user_id = :userId")
	Flux<Group> findAllByUserId(Integer userId);

	@Query("SELECT g.*, gu.* "
		+ "   FROM groups g "
		+ "	  JOIN group_user gu "
		+ "     ON g.group_id = gu.group_id "
		+ "	 WHERE g.group_id = :id AND gu.user_id = :userId")
	Mono<GroupWithUserInfo> findByIdWithUserInfo(Integer id, Integer userId);
}
