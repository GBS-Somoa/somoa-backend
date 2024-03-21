package com.somoa.serviceback.domain.device.repository;

import com.somoa.serviceback.domain.device.entity.Device;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DeviceRepository extends ReactiveCrudRepository<Device, Integer> {

    Mono<Device> findByCode(String code);
}
