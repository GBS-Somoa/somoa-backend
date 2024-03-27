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

import java.time.Duration;
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

    public Flux<Object> searchSupply(Integer groupId, Boolean careRequired) {
        return deviceSupplyRepository.findDistinctSupplyIdsByGroupId(groupId)
                .flatMap(supplyId -> supplyRepository.findById(supplyId))
                .filter(supply -> {
                    boolean conditionMet = isCareNeeded(supply);
                    return careRequired ? conditionMet : !conditionMet;
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

    public static boolean isCareNeeded(Supply supply) {
        Map<String, Object> details = supply.getDetails();
        Map<String, Object> limits = supply.getSupplyLimit();

        for (Map.Entry<String, Object> entry : limits.entrySet()) {
            String key = entry.getKey();
            Object limitValue = entry.getValue();
            Object detailValue = details.get(key);
            // 날짜 비교
            if ("supplyChangeDate".equals(key)) {
                // detailValue가 Date 인스턴스인 경우, toInstant() 메소드로 변환
                if (detailValue instanceof Date) {
                    Instant detailDate = ((Date) detailValue).toInstant();
                    Instant now = Instant.now();
                    long daysBetween = Duration.between(detailDate, now).toDays();
                    if ((int) limitValue > 0 && daysBetween >= (int) limitValue) {
                        return true;
                    }
                } else if (detailValue instanceof Instant) {
                    // detailValue가 이미 Instant 인스턴스인 경우, 직접 사용
                    Instant detailDate = (Instant) detailValue;
                    Instant now = Instant.now();
                    long daysBetween = Duration.between(detailDate, now).toDays();
                    if ((int) limitValue > 0 && daysBetween >= (int) limitValue) {
                        return true;
                    }
                }
            }
            // 상태 비교
            else if ("supplyStatus".equals(key)) {
                int detailStatusValue = FilterStatus.statusToNumber(detailValue);
                int limitStatusValue = FilterStatus.statusToNumber(limitValue);
                if (detailStatusValue < limitStatusValue && limitStatusValue > 0) {
                    return true;
                }
            }
            // 수량 비교
            else if ("supplyAmount".equals(key) && detailValue instanceof Integer && limitValue instanceof Integer) {
                if ((int) detailValue <= (int) limitValue && (int) limitValue != 0) {
                    return true;
                }
            }else if ("supplyLevel".equals(key) && detailValue instanceof Integer && limitValue instanceof Integer) {
                if(supply.getType().equals("drainTank") && (int) detailValue >= (int) limitValue && (int) limitValue != 0){
                    return true;
                }
                else if(supply.getType().equals("supplyTank") && (int) detailValue <= (int) limitValue && (int) limitValue != 0){
                    return true;
                }
            }
        }
        return false;
    }
}
