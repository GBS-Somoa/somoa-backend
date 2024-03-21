package com.somoa.serviceback.domain.device.repository;

import com.somoa.serviceback.domain.device.entity.Device;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DeviceRepository extends ReactiveCrudRepository<Device, String> {

    @Modifying
    @Query("INSERT INTO device (device_id, device_manufacturer, device_type, device_model, device_nickname, group_id) "
        + " VALUES (:#{#device.id}, :#{#device.manufacturer}, :#{#device.type}, :#{#device.model}, :#{#device.nickname}, :#{#device.groupId})")
    Mono<Void> saveForce(Device device);
}
