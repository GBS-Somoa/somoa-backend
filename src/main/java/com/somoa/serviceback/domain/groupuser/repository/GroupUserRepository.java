package com.somoa.serviceback.domain.groupuser.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.somoa.serviceback.domain.groupuser.entity.GroupUser;

public interface GroupUserRepository extends ReactiveCrudRepository<GroupUser, Integer> {
}
