package com.somoa.serviceback.domain.supply.service;


import com.somoa.serviceback.domain.device.repository.DeviceRepository;
import com.somoa.serviceback.domain.group.repository.GroupUserRepository;
import com.somoa.serviceback.domain.product.dto.BarcodeRequest;
import com.somoa.serviceback.domain.product.entity.Product;
import com.somoa.serviceback.domain.product.repository.ProductRepository;
import com.somoa.serviceback.domain.supply.entity.FilterStatus;
import com.somoa.serviceback.domain.supply.entity.Supply;
import com.somoa.serviceback.domain.supply.entity.SupplyType;
import com.somoa.serviceback.domain.supply.repository.DeviceSupplyRepository;
import com.somoa.serviceback.domain.supply.repository.SupplyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplyService {

    private final DeviceRepository deviceRepository;
    private final DeviceSupplyRepository deviceSupplyRepository;
    private final SupplyRepository supplyRepository;
    private final GroupUserRepository groupUserRepository;
    private final ProductRepository productRepository;

    public Flux<Object> searchGroupSupply(Integer groupId, Boolean careRequired) {
        return deviceSupplyRepository.findDistinctSupplyIdsByGroupId(groupId).flatMap(supplyRepository::findById).filter(supply -> {
            boolean conditionMet = isCareNeeded(supply);
            return careRequired == conditionMet;
        }).map(supply -> {
            Map<String, Object> supplyData = new HashMap<>();
            supplyData.put("id", supply.getId());
            supplyData.put("type", supply.getType());
            supplyData.put("name", supply.getName());
            supplyData.put("supplyDetails", supply.getDetails());
            supplyData.put("supplyLimit", supply.getSupplyLimit());

            if (supply.getSupplyAmountTmp() != null) {
                supplyData.put("supplyAmountTmp", supply.getSupplyAmountTmp());
            }
            return supplyData;
        });
    }

    public Mono<Object> searchAllGroupSupply(Integer userId,Integer groupId) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalCount", 0);
        Map<String, ArrayList<Object>> careNeeded = new HashMap<>();
        Map<String, ArrayList<Object>> careNotNeeded = new HashMap<>();
        for (String action : SupplyType.getActions()) {
            careNeeded.put(action, new ArrayList<>());
            careNotNeeded.put(action, new ArrayList<>());
        }
        resultMap.put("isCareNeeded", careNeeded);
        resultMap.put("isCareNotNeeded", careNotNeeded);

        AtomicInteger totalCount = new AtomicInteger(0);

        Set<String> processedSupplyIds = new HashSet<>();
        return groupUserRepository.findGroupIdsByUserId(userId).collectList().flatMap(userGroupIds -> {
            if (!userGroupIds.contains(groupId)) {
                return Mono.error(new RuntimeException("속한 그룹이 아닙니다!"));
            }
            return deviceRepository.findDeviceIdsByGroupId(groupId).collectList().flatMap(deviceIds -> {
                if(deviceIds.isEmpty()){return Mono.just(resultMap);}
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
                        boolean careNeededcheck = isCareNeeded(supply);
                        totalCount.incrementAndGet();
                        return  Pair.of(careNeededcheck, supplyData);
                    });
                }).collectList().flatMap(list -> {
                    List<Pair<Boolean, Map<String, Object>>> sortedList = list.stream()
                            .sorted(Comparator.comparing(pair -> (String) pair.getSecond().get("supplyId")))
                            .collect(Collectors.toList());

                    resultMap.put("totalCount", totalCount.intValue());
                    for (Pair<Boolean, Map<String, Object>> pair : sortedList) {
                        boolean careNeededcheck = pair.getFirst();
                        Map<String, Object> supplyData = pair.getSecond();
                        String action = SupplyType.getActionForType((String) supplyData.get("supplyType"));
                        if (careNeededcheck) {
                            ((Map<String, ArrayList<Object>>) resultMap.get("isCareNeeded")).get(action).add(supplyData);
                        } else {
                            ((Map<String, ArrayList<Object>>) resultMap.get("isCareNotNeeded")).get(action).add(supplyData);
                        }
                    }
                    return Mono.just(resultMap);
                });
            });
        });
    }

    public Mono<Object> searchAllSupply(Integer userId) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalCount", 0);
        Map<String, ArrayList<Object>> careNeeded = new HashMap<>();
        Map<String, ArrayList<Object>> careNotNeeded = new HashMap<>();
        for (String action : SupplyType.getActions()) {
            careNeeded.put(action, new ArrayList<>());
            careNotNeeded.put(action, new ArrayList<>());
        }
        resultMap.put("isCareNeeded", careNeeded);
        resultMap.put("isCareNotNeeded", careNotNeeded);

        AtomicInteger totalCount = new AtomicInteger(0);
        Set<String> processedSupplyIds = new HashSet<>();

        return groupUserRepository.findGroupIdsByUserId(userId).collectList().
                flatMap(groupIds -> {
                    if(groupIds.isEmpty()){return Mono.just(resultMap);}
            return deviceRepository.findDeviceIdsByGroupIds(groupIds)
                    .collectList().flatMap(deviceIds -> {
                        if(deviceIds.isEmpty()){return Mono.just(resultMap);}
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
                        boolean careNeededcheck = isCareNeeded(supply);
                        totalCount.incrementAndGet();
                        return  Pair.of(careNeededcheck, supplyData);
                    });
                }).collectList()
                .flatMap(list -> {
                    List<Pair<Boolean, Map<String, Object>>> sortedList = list.stream()
                            .sorted(Comparator.comparing(pair -> (String) pair.getSecond().get("supplyId")))
                            .collect(Collectors.toList());

                    resultMap.put("totalCount", totalCount.intValue());

                    for (Pair<Boolean, Map<String, Object>>  pair : sortedList) {
                        boolean careNeededcheck = pair.getFirst();
                        Map<String, Object> supplyData =pair.getSecond();
                        String action = SupplyType.getActionForType((String) supplyData.get("supplyType"));
                        if (careNeededcheck) {
                            ((Map<String, ArrayList<Object>>) resultMap.get("isCareNeeded")).get(action).add(supplyData);
                        } else {
                            ((Map<String, ArrayList<Object>>) resultMap.get("isCareNotNeeded")).get(action).add(supplyData);
                        }
                    }
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

            switch (key) {
                case "supplyChangeDate":
                    if (isDateExceedingLimit(detailValue, limitValue)) return true;
                    break;
                case "supplyStatus":
                    if (isStatusBelowLimit(detailValue, limitValue)) return true;
                    break;
                case "supplyAmount":
                case "supplyLevel":
                    if (isValueOutsideLimit(supply, key, detailValue, limitValue)) return true;
                    break;
            }
        }
        return false;
    }

    private static boolean isDateExceedingLimit(Object detailValue, Object limitValue) {
        if (!(limitValue instanceof Integer)) return false;
        int limitDays = (int) limitValue;
        if (limitDays <= 0) return false;

        Instant detailDate = detailValue instanceof Date ?
                ((Date) detailValue).toInstant() :
                detailValue instanceof Instant ? (Instant) detailValue : null;
        if (detailDate == null) return false;

        long daysBetween = Duration.between(detailDate, Instant.now()).toDays();
        return daysBetween >= limitDays;
    }

    private static boolean isStatusBelowLimit(Object detailValue, Object limitValue) {
        int detailStatusValue = FilterStatus.statusToNumber(detailValue);
        int limitStatusValue = FilterStatus.statusToNumber(limitValue);
        return detailStatusValue >= limitStatusValue && limitStatusValue > 0;
    }

    private static boolean isValueOutsideLimit(Supply supply, String key, Object detailValue, Object limitValue) {
        if (!(detailValue instanceof Integer) || !(limitValue instanceof Integer)) return false;
        int detailIntValue = (int) detailValue;
        int limitIntValue = (int) limitValue;

        switch (key) {
            case "supplyAmount":
                return detailIntValue <= limitIntValue && limitIntValue != 0;
            case "supplyLevel":
                if (supply.getType().equals("drainTank")) {
                    return detailIntValue >= limitIntValue && limitIntValue != 100;
                } else if (supply.getType().equals("supplyTank")) {
                    return detailIntValue <= limitIntValue && limitIntValue != 0;
                }
                break;
        }
        return false;
    }

    public Mono<Product> barcodeToProduct(Integer loginUserId, BarcodeRequest barcodeRequest) {
        return productRepository.findByBarcode(barcodeRequest.getBarcode())
                .switchIfEmpty(Mono.error(new RuntimeException("해당 바코드에 대한 제품이 존재하지 않습니다.")));
    }
}
