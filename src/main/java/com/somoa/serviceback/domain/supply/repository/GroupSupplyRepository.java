package com.somoa.serviceback.domain.supply.repository;

import com.somoa.serviceback.domain.supply.entity.GroupSupply;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GroupSupplyRepository extends ReactiveCrudRepository<GroupSupply, Integer> {

    @Query("SELECT supply_id FROM group_supply WHERE group_id = :groupId")
    Flux<String> findSupplyIdsByGroupId(Integer groupId);

    Mono<Boolean> existsBySupplyId(String supplyId);
}
