package com.somoa.serviceback.domain.supplies.service;


import com.somoa.serviceback.domain.device.repository.DeviceRepository;
import com.somoa.serviceback.domain.devicesupplies.repository.DeviceSuppliesRepository;
import com.somoa.serviceback.domain.supplies.entity.Supplies;
import com.somoa.serviceback.domain.supplies.repository.SuppliesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SuppliesService {

    private final DeviceRepository deviceRepository;
    private final DeviceSuppliesRepository deviceSuppliesRepository;
    private final SuppliesRepository suppliesRepository;

    public Flux<Map<Integer, Supplies>> findSuppliesByGroupIdAndCareRequired(Integer groupId, Boolean careRequired) {
        return deviceRepository.findAllByGroupId(groupId)
                .flatMap(device -> deviceSuppliesRepository.findAllByDeviceId(device.getId()))
                .flatMap(deviceSupplies -> suppliesRepository.findById(deviceSupplies.getSuppliesId()))
                /**
                 *  Todo:조건수정 해야함
                 *  supplies의 limit와 careRequired를 통해 조건문 수정 필요
                 */
                .filter(supplies -> careRequired) // Simplified for illustration
                .index()
                .map(indexedTuple -> Map.of(indexedTuple.getT1().intValue() + 1, indexedTuple.getT2()));
    }

}