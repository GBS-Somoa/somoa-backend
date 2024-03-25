package com.somoa.serviceback.domain.supplies.repository;

import com.somoa.serviceback.domain.supplies.entity.Supplies;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface SuppliesRepository extends ReactiveMongoRepository<Supplies, String> {

}
