package com.somoa.serviceback.domain.group.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.somoa.serviceback.domain.group.entity.Group;

public interface GroupRepository extends ReactiveCrudRepository<Group, Integer> {
}
