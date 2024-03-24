package com.somoa.serviceback.domain.supply.repository;

import com.somoa.serviceback.domain.supply.entity.DeviceSupply;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface DeviceSupplyRepository extends ReactiveCrudRepository<DeviceSupply, Integer> {
}
