package com.somoa.serviceback.domain.supply.repository;

import com.somoa.serviceback.domain.supply.entity.DeviceSupply;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeviceSupplyRepository extends ReactiveCrudRepository<DeviceSupply, Integer> {

    @Query("SELECT supply_id FROM device_supply WHERE device_id = :deviceId")
    Flux<String> findSupplyIdsByDeviceId(String deviceId);

    Mono<Void> deleteByDeviceId(String deviceId);
}
