package com.somoa.serviceback.domain.group.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.somoa.serviceback.domain.group.entity.Group;

import reactor.core.publisher.Flux;

public interface GroupRepository extends ReactiveCrudRepository<Group, Integer> {

	@Query("SELECT * "
		+ "   FROM group g "
		+ "   JOIN group_user gu "
		+ "     ON g.group_id = gu.group_id "
		+ "  WHERE gu.user_id = :userId")
	Flux<Group> findAllByUserId(Integer userId);
}
