package com.somoa.serviceback.domain.supply.service;


import com.somoa.serviceback.domain.device.repository.DeviceRepository;
import com.somoa.serviceback.domain.supply.entity.FilterStatus;
import com.somoa.serviceback.domain.supply.entity.Supply;
import com.somoa.serviceback.domain.supply.repository.DeviceSupplyRepository;
import com.somoa.serviceback.domain.supply.repository.GroupSupplyRepository;
import com.somoa.serviceback.domain.supply.repository.SupplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SupplyService {

    private final DeviceRepository deviceRepository;
    private final DeviceSupplyRepository deviceSupplyRepository;
    private final SupplyRepository supplyRepository;

    /**
     * Todo: 로직  정리필요. 추후 DeviceController에서도 사용예정
     * @param groupId
     * @param careRequired
     * @return
     */
    public Flux<Object> searchSupply(Integer groupId, Boolean careRequired) {
        return deviceRepository.findAllByGroupId(groupId)
                .flatMap(device -> deviceSupplyRepository.findSupplyIdsByDeviceId(device.getId()))
                .flatMap(supplyId -> supplyRepository.findById(supplyId))
                .filter(supply -> {
                    boolean conditionMet = false; // 조건을 만족하는지 여부를 저장할 변수
                    if (!conditionMet &&supply.getDetails().containsKey("supplyChangeDate")) {
                        Instant changeDateLimit = Instant.now().minusSeconds(((Integer) supply.getSupplyLimit().get("supplyChangeDate")) * 86400); // 일수를 초로 변환
                        Instant changeDate = ((Date) supply.getDetails().get("supplyChangeDate")).toInstant(); // 교체날짜
                        conditionMet = changeDate.isBefore(changeDateLimit);
                    }
                    if (!conditionMet &&supply.getDetails().containsKey("supplyStatus")) {
                        int status = FilterStatus.statusToNumber((String) supply.getDetails().get("supplyStatus"));
                        int limitStatus = FilterStatus.statusToNumber((String) supply.getSupplyLimit().get("supplyStatus"));
                        conditionMet = (limitStatus > status && limitStatus != 0);
                    }
                    if (!conditionMet &&supply.getDetails().containsKey("supplyAmount")) {
                        Integer amount = (Integer) supply.getDetails().get("supplyAmount");
                        Integer limitAmount = (Integer) supply.getSupplyLimit().get("supplyAmount");
                        conditionMet = (amount <= limitAmount && limitAmount!=0);
                    }
                    return careRequired ? conditionMet : !conditionMet; // careRequired가 true면 조건 만족 시, false면 조건 불만족 시 항목 포함
                })
                .index()
                .map(indexedTuple -> {
                    Supply supply = indexedTuple.getT2();
                    Map<Object, Object> data = new HashMap<>();
                    data.put("id", supply.getId());
                    data.put("type", supply.getType());
                    data.put("name", supply.getName());
                    return Map.of(indexedTuple.getT1().intValue() + 1, data);
                });
    }

    public Mono<Boolean> updateSupply(String supplyId, Integer supplyAmount) {
        return supplyRepository.findById(supplyId)
                .flatMap(supply -> {
                    if (supply.getDetails().containsKey("supplyAmount")) {
                        supply.getDetails().put("supplyAmount", supplyAmount);
                        return supplyRepository.save(supply).map(savedSupply -> true);
                    } else {
                        return Mono.error(new RuntimeException("소모용량이 없는 소모품입니다."));
                    }
                })
                .defaultIfEmpty(false);
    }
}