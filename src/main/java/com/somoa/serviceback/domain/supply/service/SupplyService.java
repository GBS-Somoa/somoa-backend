package com.somoa.serviceback.domain.supply.service;


import com.somoa.serviceback.domain.device.repository.DeviceRepository;
import com.somoa.serviceback.domain.group.repository.GroupUserRepository;
import com.somoa.serviceback.domain.supply.entity.FilterStatus;
import com.somoa.serviceback.domain.supply.entity.Supply;
import com.somoa.serviceback.domain.supply.entity.SupplyType;
import com.somoa.serviceback.domain.supply.repository.DeviceSupplyRepository;
import com.somoa.serviceback.domain.supply.repository.SupplyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplyService {

    private final DeviceRepository deviceRepository;
    private final DeviceSupplyRepository deviceSupplyRepository;
    private final SupplyRepository supplyRepository;
    private final GroupUserRepository groupUserRepository;

    public Flux<Object> searchGroupSupply(Integer groupId, Boolean careRequired) {
        return deviceSupplyRepository.findDistinctSupplyIdsByGroupId(groupId).flatMap(supplyId -> supplyRepository.findById(supplyId)).filter(supply -> {
            boolean conditionMet = isCareNeeded(supply);
            return careRequired == conditionMet;
        }).map(supply -> {
            Map<String, Object> supplyData = new HashMap<>();
            supplyData.put("id", supply.getId());
            supplyData.put("type", supply.getType());
            supplyData.put("name", supply.getName());
            supplyData.put("supplyDetails", supply.getDetails());
            supplyData.put("supplyLimit", supply.getSupplyLimit());

            // supplyAmountTmp가 null이 아닐 때만 값을 포함
            if (supply.getSupplyAmountTmp() != null) {
                supplyData.put("supplyAmountTmp", supply.getSupplyAmountTmp());
            }
            return supplyData;
        });
    }

    public Mono<Object> searchAllGroupSupply(Integer userId,Integer groupId) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> supplyDataMap = new HashMap<>();
        Map<String, ArrayList<Object>> careNeeded = new HashMap<>();
        careNeeded.put("clean", new ArrayList<>());
        careNeeded.put("replace", new ArrayList<>());
        careNeeded.put("charge", new ArrayList<>());
        Map<String, ArrayList<Object>> careNotNeeded = new HashMap<>();
        careNotNeeded.put("clean", new ArrayList<>());
        careNotNeeded.put("replace", new ArrayList<>());
        careNotNeeded.put("charge", new ArrayList<>());
        supplyDataMap.put("isCareNeeded", careNeeded);
        supplyDataMap.put("isCareNotNeeded", careNotNeeded);
        AtomicInteger totalCount = new AtomicInteger(0);

        Set<String> processedSupplyIds = new HashSet<>(); // 중복된 supplyId를 추적하기 위한 Set
        return groupUserRepository.findGroupIdsByUserId(userId).collectList().flatMap(userGroupIds -> {
            if (!userGroupIds.contains(groupId)) {
                return Mono.error(new RuntimeException("속한 그룹이 아닙니다!"));
            }
            return deviceRepository.findDeviceIdsByGroupId(groupId).collectList().flatMap(deviceIds -> {
                return deviceSupplyRepository.findDistinctSuppliesByDeviceIds(deviceIds).flatMap(supplyWithGroupInfo -> {
                    if (!processedSupplyIds.add(supplyWithGroupInfo.getSupplyId())) {
                        return Mono.empty();
                    }
                    return supplyRepository.findById(supplyWithGroupInfo.getSupplyId()).map(supply -> {
                        Map<String, Object> supplyData = new HashMap<>();
                        supplyData.put("supplyId", supply.getId());
                        supplyData.put("supplyType", supply.getType());
                        supplyData.put("supplyName", supply.getName());
                        supplyData.put("supplyDetails", supply.getDetails());
                        supplyData.put("supplyLimit", supply.getSupplyLimit());
                        supplyData.put("groupId", supplyWithGroupInfo.getGroupId());
                        supplyData.put("groupName", supplyWithGroupInfo.getGroupName());
                        supplyData.put("deviceId", supplyWithGroupInfo.getDeviceId());
                        supplyData.put("deviceNickname", supplyWithGroupInfo.getDeviceNickname());

                        if (supply.getSupplyAmountTmp() != null) {
                            supplyData.put("supplyAmountTmp", supply.getSupplyAmountTmp());
                        }
                        // 관리 필요 여부 판단
                        boolean careNeededcheck = isCareNeeded(supply);

                        String action = SupplyType.getActionForType(supply.getType());
                        if (!careNeededcheck) {
                            ((Map<String, ArrayList<Object>>) supplyDataMap.get("isCareNeeded")).get(action).add(supplyData);
                        } else {
                            ((Map<String, ArrayList<Object>>) supplyDataMap.get("isCareNotNeeded")).get(action).add(supplyData);
                        }
                        totalCount.incrementAndGet();
                        return supplyData;
                    });
                }).collectList().flatMap(list -> {
                    resultMap.put("totalCount", totalCount.intValue());
                    resultMap.put("isCareNeeded", supplyDataMap.get("isCareNeeded"));
                    resultMap.put("isCareNotNeeded", supplyDataMap.get("isCareNotNeeded"));


                    return Mono.just(resultMap);
                }); // 최종 결과 반환
            });
        });
    }

    public Mono<Object> searchAllSupply(Integer userId) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> supplyDataMap = new HashMap<>();
        Map<String, ArrayList<Object>> careNeeded = new HashMap<>();
        careNeeded.put("clean", new ArrayList<>());
        careNeeded.put("replace", new ArrayList<>());
        careNeeded.put("charge", new ArrayList<>());

        Map<String, ArrayList<Object>> careNotNeeded = new HashMap<>();
        careNotNeeded.put("clean", new ArrayList<>());
        careNotNeeded.put("replace", new ArrayList<>());
        careNotNeeded.put("charge", new ArrayList<>());

        supplyDataMap.put("isCareNeeded", careNeeded);
        supplyDataMap.put("isCareNotNeeded", careNotNeeded);

        AtomicInteger totalCount = new AtomicInteger(0);

        Set<String> processedSupplyIds = new HashSet<>(); // 중복된 supplyId를 추적하기 위한 Set

        return groupUserRepository.findGroupIdsByUserId(userId).collectList().flatMap(groupIds -> {
            return deviceRepository.findDeviceIdsByGroupIds(groupIds).collectList().flatMap(deviceIds -> {
                return deviceSupplyRepository.findDistinctSuppliesByDeviceIds(deviceIds).flatMap(supplyWithGroupInfo -> {
                    if (!processedSupplyIds.add(supplyWithGroupInfo.getSupplyId())) { // 중복 supplyId 제거
                        return Mono.empty();
                    }
                    return supplyRepository.findById(supplyWithGroupInfo.getSupplyId()).map(supply -> {
                        Map<String, Object> supplyData = new HashMap<>();
                        supplyData.put("supplyId", supply.getId());
                        supplyData.put("supplyType", supply.getType());
                        supplyData.put("supplyName", supply.getName());
                        supplyData.put("supplyDetails", supply.getDetails());
                        supplyData.put("supplyLimit", supply.getSupplyLimit());
                        supplyData.put("groupId", supplyWithGroupInfo.getGroupId());
                        supplyData.put("groupName", supplyWithGroupInfo.getGroupName());
                        supplyData.put("deviceId", supplyWithGroupInfo.getDeviceId());
                        supplyData.put("deviceNickname", supplyWithGroupInfo.getDeviceNickname());

                        if (supply.getSupplyAmountTmp() != null) {
                            supplyData.put("supplyAmountTmp", supply.getSupplyAmountTmp());
                        }
                        boolean careNeededcheck = isCareNeeded(supply);
                        String action = SupplyType.getActionForType(supply.getType());
                        if (!careNeededcheck) {
                            ((Map<String, ArrayList<Object>>) supplyDataMap.get("isCareNeeded")).get(action).add(supplyData);
                        } else {
                            ((Map<String, ArrayList<Object>>) supplyDataMap.get("isCareNotNeeded")).get(action).add(supplyData);
                        }
                        totalCount.incrementAndGet();
                        return supplyData;
                    });
                }).collectList().flatMap(list -> {
                    resultMap.put("totalCount", totalCount.intValue());
                    resultMap.put("isCareNeeded", supplyDataMap.get("isCareNeeded"));
                    resultMap.put("isCareNotNeeded", supplyDataMap.get("isCareNotNeeded"));


                    return Mono.just(resultMap);
                });
            });
        });
    }


    public Mono<Boolean> updateSupply(String supplyId, Integer supplyAmount) {
        return supplyRepository.findById(supplyId)
                .switchIfEmpty(Mono.error(new RuntimeException("존재하지 않는 소모품 ID입니다.")))
                .flatMap(supply -> {
                    if (supply.getDetails().containsKey("supplyAmount")) {
                        supply.getDetails().put("supplyAmount", supplyAmount);
                        return supplyRepository.save(supply).map(savedSupply -> true);
                    } else {
                        return Mono.error(new RuntimeException("소모용량이 없는 소모품입니다."));
                    }
                });
    }

    @Transactional
    public Mono<Supply> updateSupplyLimit(String supplyId, Map<String, Object> newLimit) {
        if (!newLimit.containsKey("supplyLimit")) {
            return Mono.error(new IllegalArgumentException("supplyLimit 키가 요청에 포함되어 있지 않습니다."));
        }

        @SuppressWarnings("unchecked") // 타입 캐스팅 경고를 억제
        Map<String, Object> supplyLimit = (Map<String, Object>) newLimit.get("supplyLimit");

        return supplyRepository.findById(supplyId)
                .switchIfEmpty(Mono.error(new RuntimeException("존재하지 않는 소모품 ID입니다.")))
                .flatMap(supply -> {
                    Map<String, Object> currentLimit = supply.getSupplyLimit();
                    boolean allKeysExist = supplyLimit.keySet().stream()
                            .allMatch(currentLimit::containsKey);
                    if (!allKeysExist) {
                        return Mono.error(new DataIntegrityViolationException("하나 이상의 키가 존재하지 않습니다."));
                    }
                    for (Map.Entry<String, Object> entry : supplyLimit.entrySet()) {
                        if (!SupplyType.validateSupplyLimitKey(supply.getType(),entry.getKey(), entry.getValue())) {
                            return Mono.error(new IllegalArgumentException("유효하지 않은 키 또는 값입니다: " + entry.getKey()));
                        }
                    }
                    currentLimit.putAll(supplyLimit);
                    supply.setSupplyLimit(currentLimit);
                    return supplyRepository.save(supply);
                });
    }

    public static boolean isCareNeeded(Supply supply) {
        Map<String, Object> details = supply.getDetails();
        Map<String, Object> limits = supply.getSupplyLimit();

        for (Map.Entry<String, Object> entry : limits.entrySet()) {
            String key = entry.getKey();
            Object limitValue = entry.getValue();
            Object detailValue = details.get(key);
            if ("supplyChangeDate".equals(key)) { //교체필요
                if (detailValue instanceof Date) {
                    Instant detailDate = ((Date) detailValue).toInstant();
                    Instant now = Instant.now();
                    long daysBetween = Duration.between(detailDate, now).toDays();
                    if ((int) limitValue > 0 && daysBetween >= (int) limitValue) {
                        return true;
                    }
                } else if (detailValue instanceof Instant detailDate) {
                    // detailValue가 이미 Instant 인스턴스인 경우, 직접 사용
                    Instant now = Instant.now();
                    long daysBetween = Duration.between(detailDate, now).toDays();
                    if ((int) limitValue > 0 && daysBetween >= (int) limitValue) {
                        return true;
                    }
                }
            }
            // 상태 비교
            else if ("supplyStatus".equals(key)) { // 청소필요
                int detailStatusValue = FilterStatus.statusToNumber(detailValue);
                int limitStatusValue = FilterStatus.statusToNumber(limitValue);
                if (detailStatusValue <= limitStatusValue && limitStatusValue > 0) {
                    return true;
                }
            }
            // 수량 비교
            else if ("supplyAmount".equals(key) && detailValue instanceof Integer && limitValue instanceof Integer) { // 충전
                if ((int) detailValue <= (int) limitValue && (int) limitValue != 0) {
                    return true;
                }
            } else if ("supplyLevel".equals(key) && detailValue instanceof Integer && limitValue instanceof Integer) { //
                if (supply.getType().equals("drainTank") && (int) detailValue >= (int) limitValue && (int) limitValue != 100) {
                    return true; // 청소
                } else if (supply.getType().equals("supplyTank") && (int) detailValue <= (int) limitValue && (int) limitValue != 0) {
                    return true; //충전
                }
            }
        }
        return false;
    }
}
