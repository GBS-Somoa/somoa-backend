package com.somoa.serviceback.domain.supply.service;


import com.somoa.serviceback.domain.device.repository.DeviceRepository;
import com.somoa.serviceback.domain.group.entity.GroupUser;
import com.somoa.serviceback.domain.group.repository.GroupUserRepository;
import com.somoa.serviceback.domain.supply.dto.SupplyWithGroupInfo;
import com.somoa.serviceback.domain.supply.entity.FilterStatus;
import com.somoa.serviceback.domain.supply.entity.Supply;
import com.somoa.serviceback.domain.supply.repository.DeviceSupplyRepository;
import com.somoa.serviceback.domain.supply.repository.GroupSupplyRepository;
import com.somoa.serviceback.domain.supply.repository.SupplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SupplyService {

    private final DeviceRepository deviceRepository;
    private final DeviceSupplyRepository deviceSupplyRepository;
    private final SupplyRepository supplyRepository;
    private final GroupUserRepository groupUserRepository;

    public Flux<Object> searchGroupSupply(Integer groupId, Boolean careRequired) {
        return deviceSupplyRepository.findDistinctSupplyIdsByGroupId(groupId)
                .flatMap(supplyId -> supplyRepository.findById(supplyId))
                .filter(supply -> {
                    boolean conditionMet = isCareNeeded(supply);
                    return careRequired ? conditionMet : !conditionMet;
                })
                .map(supply -> {
                    Map<String, Object> supplyData = new HashMap<>();
                    supplyData.put("id", supply.getId());
                    supplyData.put("type", supply.getType());
                    supplyData.put("name", supply.getName());
                    supplyData.put("details", supply.getDetails());
                    supplyData.put("supplyLimit", supply.getSupplyLimit());

                    // supplyAmountTmp가 null이 아닐 때만 값을 포함
                    if (supply.getSupplyAmountTmp() != null) {
                        supplyData.put("supplyAmountTmp", supply.getSupplyAmountTmp());
                    }
                    return supplyData;
                });
    }

    public  Flux<Map<String, List<Object>>> searchAllSupply(Integer userId) {
        System.out.println("@1@");
        Map<String, List<Object>> resultMap = new HashMap<>();
        resultMap.put("isCareNeeded", new ArrayList<>());
        resultMap.put("isCareNotNeeded", new ArrayList<>());

        // 1. 사용자 ID를 기반으로 해당 사용자가 속한 모든 그룹 조회
        return groupUserRepository.findGroupIdsByUserId(userId)
                .flatMapMany(Flux::fromIterable) // Flux<Group>로 변환
                .flatMap(group ->
                        // 2. 각 그룹별 Supply ID 조회
                        deviceSupplyRepository.findDistinctSupplyIdsByGroupId(group.getId())
                                .flatMap(supplyId ->
                                        // 3. Supply 정보 조회
                                        supplyRepository.findById(supplyId)
                                                .map(supply -> new AbstractMap.SimpleEntry<>(group, supply))
                                )
                )
                .collectList() // 모든 결과를 List로 수집
                .flatMap(list -> {
                    // 4. 수집된 결과를 isCareNeeded 여부에 따라 분류
                    list.forEach(entry -> {
                        Group group = entry.getKey();
                        Supply supply = entry.getValue();
                        boolean careNeeded = isCareNeeded(supply);

                        Object supplyWithDeviceInfo = createSupplyWithDeviceInfo(supply, group);

                        if (careNeeded) {
                            resultMap.get("isCareNeeded").add(supplyWithDeviceInfo);
                        } else {
                            resultMap.get("isCareNotNeeded").add(supplyWithDeviceInfo);
                        }
                    });
                    return Mono.just(resultMap);
                });
    }

    private Supply convertToSupply(SupplyWithGroupInfo supplyWithGroupInfo) {
        // SupplyWithGroupInfo에서 groupName과 groupId를 제외하고 Supply 객체 생성 로직
        return Supply.builder()
                .id(supplyWithGroupInfo.getId())
                .type(supplyWithGroupInfo.getType())
                .name(supplyWithGroupInfo.getName())
                .details(supplyWithGroupInfo.getDetails())
                .supplyLimit(supplyWithGroupInfo.getSupplyLimit())
                .supplyAmountTmp(supplyWithGroupInfo.getSupplyAmountTmp())
                .build();
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
