package com.somoa.serviceback.domain.device.repository;

import com.somoa.serviceback.domain.group.entity.Group;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.somoa.serviceback.domain.device.entity.Device;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DeviceRepository extends ReactiveCrudRepository<Device, String> {

    @Modifying
    @Query("INSERT INTO device (device_id, device_manufacturer, device_type, device_model, device_nickname, group_id) "
        + " VALUES (:#{#device.id}, :#{#device.manufacturer}, :#{#device.type}, :#{#device.model}, :#{#device.nickname}, :#{#device.groupId})")
    Mono<Void> saveForce(Device device);

    @Query("SELECT g.* "
            + "   FROM `group` AS g "
            + "	  JOIN device AS d "
            + "     ON g.group_id = d.group_id "
            + "	 WHERE d.device_id = :deviceId")
    Mono<Group> findGroupByDeviceId(String deviceId);

    @Query("SELECT d.group_id FROM device d WHERE d.device_id = :deviceId")
    Mono<String> findGroupIdByDeviceId(String deviceId);

    @Query("SELECT device_id FROM device WHERE group_id IN (:groupIds)")
    Flux<String> findDeviceIdsByGroupIds(List<Integer> groupIds);

    @Query("SELECT * FROM device WHERE group_id =:groupId ORDER BY created_at ASC")
    Flux<Device> findAllByGroupId(Integer groupId);

    @Query("SELECT device_id FROM device WHERE group_id = :groupId")
    Flux<String> findDeviceIdsByGroupId(Integer groupId);

    @Query("SELECT d.* FROM device d " +
            "INNER JOIN ( " +
            "   SELECT ds.device_id FROM device_supply ds " +
            "   WHERE ds.supply_id = :supplyId " +
            "   ORDER BY ds.updated_at DESC " +
            "   LIMIT 1 " +
            ") AS latest_device ON d.device_id = latest_device.device_id")
    Mono<Device> findFirstDeviceBySupplyId(String supplyId);
}
