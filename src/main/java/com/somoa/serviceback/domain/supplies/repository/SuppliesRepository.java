package com.somoa.serviceback.domain.supplies.repository;

import com.somoa.serviceback.domain.supplies.entity.Supplies;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface SuppliesRepository extends ReactiveCrudRepository<Supplies, String> {
}
