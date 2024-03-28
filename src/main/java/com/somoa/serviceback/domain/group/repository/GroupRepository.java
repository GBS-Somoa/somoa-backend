package com.somoa.serviceback.domain.group.repository;

import com.somoa.serviceback.domain.group.dto.GroupResponse;
import com.somoa.serviceback.domain.group.entity.Group;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface GroupRepository extends ReactiveCrudRepository<Group, Integer> {

	@Query("SELECT g.*, gu.* "
		+ "	     , gmu.user_id AS manager_id "
		+ "	     , mu.user_username AS manager_username "
		+ "	     , mu.user_nickname AS manager_nickname "
		+ "   FROM `group` g "
		+ "   JOIN group_user gu "
		+ "     ON g.group_id = gu.group_id "
		+ "   JOIN group_user gmu "
		+ "     ON g.group_id = gmu.group_id AND gmu.role = '관리자' "
		+ "   JOIN user mu "
		+ "     ON gmu.user_id = mu.user_id "
		+ "  WHERE gu.user_id = :userId "
		+ "  ORDER BY gu.ordered_num ASC ")
	Flux<GroupResponse> findAllByUserId(Integer userId);
}
