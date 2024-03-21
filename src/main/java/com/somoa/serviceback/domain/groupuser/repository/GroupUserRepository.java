package com.somoa.serviceback.domain.groupuser.repository;

import com.somoa.serviceback.domain.groupuser.entity.GroupUser;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface GroupUserRepository extends ReactiveCrudRepository<GroupUser, Integer> {
}
