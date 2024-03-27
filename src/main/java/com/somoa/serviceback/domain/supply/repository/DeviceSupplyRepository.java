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


    @Query("SELECT DISTINCT s.supply_id, s.supply_type, s.supply_name, s.details, s.supply_limit, g.group_id, g.group_name " +
            "FROM supply s " +
            "INNER JOIN device_supply ds ON s.supply_id = ds.supply_id " +
            "INNER JOIN device d ON ds.device_id = d.device_id " +
            "INNER JOIN `group` g ON d.group_id = g.group_id " +
            "INNER JOIN group_user gu ON g.group_id = gu.group_id " +
            "WHERE gu.user_id = :userId")
    Flux<SupplyWithGroupInfo> findDistinctSuppliesByUserId(Integer userId);


    Flux<Object> findSupplyIdsByGroupId(Integer id);
}
