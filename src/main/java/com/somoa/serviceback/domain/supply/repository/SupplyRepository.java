package com.somoa.serviceback.domain.supply.repository;

import com.somoa.serviceback.domain.supply.entity.Supply;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface SupplyRepository extends ReactiveMongoRepository<Supply, String> {
}
