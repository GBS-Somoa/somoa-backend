package com.somoa.serviceback.domain.supply.repository;

import com.somoa.serviceback.domain.supply.dto.SupplyWithGroupInfo;
import com.somoa.serviceback.domain.supply.entity.DeviceSupply;
import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DeviceSupplyRepository extends ReactiveCrudRepository<DeviceSupply, Integer> {

    @Query("SELECT supply_id FROM device_supply WHERE device_id = :deviceId")
    Flux<String> findSupplyIdsByDeviceId(String deviceId);

    Mono<Void> deleteAllByDeviceId(String deviceId);

    Flux<DeviceSupply> findAllByDeviceId(String deviceId);

    @Query("SELECT supply_id FROM device_supply WHERE device_id = :deviceId")
    Flux<String> findAllSupplyIdByDeviceId(String deviceId);

    @Query("SELECT DISTINCT ds.supply_id FROM device_supply ds INNER JOIN device d ON ds.device_id = d.device_id WHERE d.group_id = :groupId")
    Flux<String> findDistinctSupplyIdsByGroupId(@Param("groupId") Integer groupId);

    @Query("SELECT DISTINCT ds.supply_id, d.device_id, d.device_nickname, g.group_id, g.group_name " +
            "FROM device_supply ds " +
            "INNER JOIN device d ON ds.device_id = d.device_id " +
            "INNER JOIN `group` g ON d.group_id = g.group_id " +
            "WHERE ds.device_id IN (:deviceIds)")
    Flux<SupplyWithGroupInfo> findDistinctSuppliesByDeviceIds(List<String> deviceIds);

    @Query("SELECT device_id FROM device_supply WHERE supply_id = :supplyId ORDER BY updated_at  LIMIT 1")
    Mono<String> findFirstDeviceIdBySupplyId(String supplyId);


}
