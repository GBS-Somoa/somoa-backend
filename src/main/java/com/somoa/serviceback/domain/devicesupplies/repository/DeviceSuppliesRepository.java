package com.somoa.serviceback.domain.devicesupplies.repository;

import com.somoa.serviceback.domain.devicesupplies.entity.DeviceSupplies;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeviceSuppliesRepository extends ReactiveCrudRepository<DeviceSupplies, String> {

    Flux<DeviceSupplies> findAllByDeviceId(String deviceId);
}
