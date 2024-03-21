package com.somoa.serviceback.domain.group.repository;

import com.somoa.serviceback.domain.group.entity.Group;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface GroupRepository extends ReactiveCrudRepository<Group, Integer> {
}
