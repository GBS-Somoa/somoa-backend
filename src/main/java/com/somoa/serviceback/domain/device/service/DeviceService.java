package com.somoa.serviceback.domain.device.service;

import static com.somoa.serviceback.domain.supply.service.SupplyService.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.somoa.serviceback.domain.group.entity.Group;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.somoa.serviceback.domain.device.dto.DeviceApiResponse;
import com.somoa.serviceback.domain.device.dto.DeviceApiStatusResponse;
import com.somoa.serviceback.domain.device.dto.DeviceRegisterParam;
import com.somoa.serviceback.domain.device.dto.DeviceResponse;
import com.somoa.serviceback.domain.device.dto.DeviceUpdateParam;
import com.somoa.serviceback.domain.device.dto.ExternalApiResponse;
import com.somoa.serviceback.domain.device.entity.Device;
import com.somoa.serviceback.domain.device.error.DeviceErrorCode;
import com.somoa.serviceback.domain.device.exception.DeviceException;
import com.somoa.serviceback.domain.device.repository.DeviceRepository;
import com.somoa.serviceback.domain.group.entity.GroupUserRole;
import com.somoa.serviceback.domain.group.error.GroupErrorCode;
import com.somoa.serviceback.domain.group.exception.GroupException;
import com.somoa.serviceback.domain.group.repository.GroupUserRepository;
import com.somoa.serviceback.domain.supply.dto.SupplyRegisterParam;
import com.somoa.serviceback.domain.supply.dto.SupplyResponse;
import com.somoa.serviceback.domain.supply.dto.SupplyStatusParam;
import com.somoa.serviceback.domain.supply.entity.DeviceSupply;
import com.somoa.serviceback.domain.supply.entity.GroupSupply;
import com.somoa.serviceback.domain.supply.entity.Supply;
import com.somoa.serviceback.domain.supply.entity.SupplyType;
import com.somoa.serviceback.domain.supply.repository.DeviceSupplyRepository;
import com.somoa.serviceback.domain.supply.repository.GroupSupplyRepository;
import com.somoa.serviceback.domain.supply.repository.SupplyRepository;
import com.somoa.serviceback.global.config.PropertiesConfig;
import com.somoa.serviceback.global.fcm.service.FcmService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceService {

    private static final String DEVICE_API_PATH = "/api/device";
    private static final String DEVICE_ID_QUERY_PARAM = "device_id";

    private final DeviceRepository deviceRepository;
    private final GroupUserRepository groupUserRepository;
    private final SupplyRepository supplyRepository;
    private final DeviceSupplyRepository deviceSupplyRepository;
    private final GroupSupplyRepository groupSupplyRepository;
    private final PropertiesConfig propertiesConfig;
    private final FcmService fcmService;

    @Transactional
    public Mono<Map<String, Object>> save(DeviceRegisterParam param) {
        Mono<DeviceApiResponse> responseMono = getDeviceResponse(param.getCode()).cache();

        // response data
        Map<String, Object> data = new HashMap<>();
        data.put("deviceId", param.getCode());

        return deviceRepository.findById(param.getCode())
            .flatMap(existingDevice -> Mono.error(new DeviceException(DeviceErrorCode.DUPLICATE_DEVICE)))
            .switchIfEmpty(responseMono.flatMap(response ->
                                deviceRepository.saveForce(Device.builder()
                                    .id(param.getCode())
                                    .groupId(param.getGroupId())
                                    .nickname(param.getNickname())
                                    .model(response.getModel())
                                    .type(response.getType())
                                    .manufacturer(response.getManufacturer())
                                    .build())
            ))
            .then(responseMono)
            .flatMapMany(response ->
                    Flux.fromIterable(response.getSupplies())
                        .flatMap(supply -> saveSupply(param.getCode(), param.getGroupId(), supply))
            )
            .then(Mono.just(data));
    }

    private Mono<Supply> saveSupply(String deviceId, Integer groupId, SupplyRegisterParam param) {
        // 소모품 종류 유효성 검사
        if (!SupplyType.isValidType(param.getType())) {
            return Mono.error(new DeviceException(DeviceErrorCode.INVALID_DEVICE_TYPE));
        }

        Map<String, Object> details = new HashMap<>();
        Map<String, Object> supplyLimit = new HashMap<>();
        boolean amountFlag = false;
        for (String detail : param.getDetails()) {
            switch (detail) {
                case "supplyAmount":
                    amountFlag = true;
                    details.put("supplyAmount",  SupplyType.getDefaultDetail(param.getType()));
                    supplyLimit.put("supplyAmount", SupplyType.getDefaultLimit(param.getType())); // 기본값 0 -> 알람안뜨게설정
                    break;
                case "supplyStatus":
                    if (param.getType().contains("Filter")) {
                        details.put("supplyStatus",  "good");
                        supplyLimit.put("supplyStatus", "bad");
                    } else {
                        details.put("supplyStatus",  SupplyType.getDefaultDetail(param.getType()));
                        supplyLimit.put("supplyStatus", SupplyType.getDefaultLimit(param.getType()));
                    }
                    break;
                case "supplyChangeDate":
                    details.put("supplyChangeDate",Instant.now());
                    supplyLimit.put("supplyChangeDate", 365); // 기본값 365일
                    break;
                case "supplyLevel":
                    details.put("supplyLevel", SupplyType.getDefaultDetail(param.getType()));
                    supplyLimit.put("supplyLevel", SupplyType.getDefaultLimit(param.getType()));
                    break;
            }
        }
        Supply.SupplyBuilder builder = Supply.builder()
                .type(param.getType())
                .name(param.getName())
                .details(details)
                .supplyLimit(supplyLimit);

        if (amountFlag) {
            builder.supplyAmountTmp(0);
        }
        Supply newSupply = builder.build();

        // 액체류 소모품이 아닐 때 (그룹으로 관리되지 않음)
        if (!SupplyType.isLiquidType(newSupply.getType())) {
            return supplyRepository.save(newSupply)
                    .flatMap(savedSupply -> deviceSupplyRepository.save(DeviceSupply.builder()
                                    .deviceId(deviceId)
                                    .supplyId(savedSupply.getId())
                                    .build())
                            .then(Mono.just(savedSupply)));
        }

        // 액체류 소모품일 때 (그룹으로 관리됨)
        // 이미 저장되어 있는 경우, group_supply는 새로 저장되지 않음
        return getSupplyByGroupIdAndType(groupId, param.getType())
                .switchIfEmpty(supplyRepository.save(newSupply)
                        .flatMap(savedSupply -> groupSupplyRepository.save(GroupSupply.builder()
                                .groupId(groupId)
                                .supplyId(savedSupply.getId())
                                .build()).thenReturn(savedSupply)))
                .flatMap(savedSupply -> deviceSupplyRepository.save(DeviceSupply.builder()
                        .deviceId(deviceId)
                        .supplyId(savedSupply.getId())
                        .build()).thenReturn(savedSupply));
    }

    private Mono<Supply> getSupplyByGroupIdAndType(Integer groupId, String type) {
        return groupSupplyRepository.findSupplyIdsByGroupId(groupId)
                .collectList()
                .flatMapMany(Flux::fromIterable)
                .flatMap(supplyRepository::findById)
                .filter(supply -> supply.getType().equals(type))
                .next();
    }

    private Mono<DeviceApiResponse> getDeviceResponse(String deviceId) {
        WebClient webClient = WebClient.create(propertiesConfig.getManufacturerServerUrl());

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(DEVICE_API_PATH)
                        .queryParam(DEVICE_ID_QUERY_PARAM, deviceId)
                        .build())
                .header("Origin", "http://127.0.0.1:8080")
                .retrieve()
                .bodyToMono(ExternalApiResponse.class)
                .map(ExternalApiResponse::getData);
    }

    public Mono<DeviceResponse> findById(String deviceId) {
        return deviceRepository.findById(deviceId)
                .switchIfEmpty(Mono.error(new DeviceException(DeviceErrorCode.DEVICE_NOT_FOUND)))
                .flatMap(device -> deviceSupplyRepository.findSupplyIdsByDeviceId(deviceId)
                        .collectList()
                        .flatMapMany(Flux::fromIterable)
                        .flatMap(supplyRepository::findById)
                        .map(SupplyResponse::of)
                        .collectList()
                        .map(supplies -> DeviceResponse.builder()
                                .id(device.getId())
                                .nickname(device.getNickname())
                                .model(device.getModel())
                                .type(device.getType())
                                .manufacturer(device.getManufacturer())
                                .supplies(supplies)
                                .build()));
    }

    public Mono<String> update(Integer userId, String deviceId, DeviceUpdateParam param) {
        return deviceRepository.findById(deviceId)
                .switchIfEmpty(Mono.error(new DeviceException(DeviceErrorCode.DEVICE_NOT_FOUND)))
                .flatMap(device -> deviceRepository.findGroupByDeviceId(deviceId)
                        .flatMap(group -> groupUserRepository.findGroupUser(group.getId(), userId)
                                .switchIfEmpty(Mono.error(new GroupException(GroupErrorCode.USER_NOT_IN_GROUP)))
                                .flatMap(groupUser -> {
                                    if (!groupUser.getRole().equals(GroupUserRole.USER_ONLY_SUPPLY_MANAGE)) {
                                        device.setNickname(param.getDeviceName());
                                        return deviceRepository.save(device)
                                                .then(Mono.just(deviceId));
                                    } else {
                                        return Mono.error(new DeviceException(DeviceErrorCode.NO_DEVICE_MANAGEMENT_PERMISSION));
                                    }
                                })));
    }

    public Mono<String> delete(Integer userId, String deviceId) {
        return deviceRepository.findById(deviceId)
                .switchIfEmpty(Mono.error(new DeviceException(DeviceErrorCode.DEVICE_NOT_FOUND)))
                .flatMap(device -> deviceRepository.findGroupByDeviceId(deviceId)
                        .flatMap(group -> groupUserRepository.findGroupUser(group.getId(), userId)
                                .switchIfEmpty(Mono.error(new GroupException(GroupErrorCode.USER_NOT_IN_GROUP)))
                                .flatMap(groupUser -> {
                                    if (!groupUser.getRole().equals(GroupUserRole.USER_ONLY_SUPPLY_MANAGE)) {
                                        return deleteDeviceSupplies(device.getId())
                                                .then(deviceRepository.delete(device)
                                                        .then(Mono.just(deviceId)));
                                    } else {
                                        return Mono.error(new DeviceException(DeviceErrorCode.NO_DEVICE_MANAGEMENT_PERMISSION));
                                    }
                                })));
    }

    private Mono<Void> deleteDeviceSupplies(String deviceId) {
        return deviceSupplyRepository.findAllByDeviceId(deviceId)
                .flatMap(deviceSupply ->
                        groupSupplyRepository.existsBySupplyId(deviceSupply.getSupplyId())
                                .flatMap(exists -> {
                                    // 그룹으로 관리되는 소모품이 아닐 때는 소모품도 삭제
                                    if (!exists) {
                                        return supplyRepository.deleteById(deviceSupply.getSupplyId());
                                    }
                                    return Mono.empty();
                                })
                )
                .then(deviceSupplyRepository.deleteAllByDeviceId(deviceId));
    }

    public Mono<Void> statusUpdate(String deviceId, DeviceApiStatusResponse deviceApiStatusResponse) {
        // deviceId로 groupId 조회
        return deviceRepository.findGroupByDeviceId(deviceId)
                .flatMap(group ->
                        // groupId를 정상적으로 조회한 후에 다음 작업 진행
                        deviceSupplyRepository.findAllSupplyIdByDeviceId(deviceId)
                                .collectList()
                                .flatMap(supplyIds ->
                                        Flux.fromIterable(supplyIds)
                                                .flatMap(supplyId ->
                                                        updateSupplyDetails(supplyId, deviceApiStatusResponse.getSupplies(), group)
                                                )
                                                .then()
                                )
                );
    }

    @Transactional
    private Mono<Void> updateSupplyDetails(String supplyId, List<SupplyStatusParam> supplyStatusParams, Group group) {
        return supplyRepository.findById(supplyId)
                .flatMap(supply -> Flux.fromIterable(supplyStatusParams)
                        .filter(param -> (SupplyType.isLiquidType(supply.getType())&&supply.getType().equals(param.getType()))||(supply.getName().equals(param.getName()) && supply.getType().equals(param.getType())))
                        .next()
                        .flatMap(param -> updateSupply(supply, param))
                        .flatMap(supplyRepository::save)
                        .flatMap(updatedSupply -> checkLimitsAndNotify(updatedSupply, group)) // 알림 확인 및 전송
                        .then()
                );
    }


    private Mono<Supply> updateSupply(Supply supply, SupplyStatusParam param) {
        return Mono.fromCallable(() -> {
            for (int i = 0; i < param.getDetails().size(); i++) {
                String detailKey = param.getDetails().get(i);
                String newValue = param.getValues().get(i);
                if ("supplyAmount".equals(detailKey)) {
                    if (newValue != null && !newValue.isEmpty()) {
                        try {
                            Integer newAmount = Integer.parseInt(newValue);
                            Integer currentAmount = (Integer) supply.getDetails().getOrDefault(detailKey, 0);
                            Integer updatedAmount = currentAmount - newAmount;
                            if (updatedAmount < 0) {
                                throw new IllegalArgumentException("소모품양은 0보다 작을 수 없습니다.");
                            }
                            supply.getDetails().put(detailKey, updatedAmount);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("유효하지 않은 supplyAmount: " + newValue, e);
                        }
                    } else {
                        log.info("소모 데이터가 비어있음");
                    }
                } else if ("supplyChangeDate".equals(detailKey)) {
                    if (newValue != null && !newValue.isEmpty()) {
                        LocalDate localDate = LocalDate.parse(newValue);
                        LocalDateTime localDateTime = localDate.atStartOfDay();
                        Instant instantDate = localDateTime.atZone(ZoneId.of("UTC")).toInstant();
                        supply.getDetails().put(detailKey, instantDate);
                    }
                } else if ("supplyLevel".equals(detailKey)) {
                    if (newValue != null && !newValue.isEmpty()) {
                        int intValue = Integer.parseInt(newValue);
                        supply.getDetails().put(detailKey, intValue);
                    }
                }else if("supplyStatus".equals(detailKey)){
                    if (newValue != null && !newValue.isEmpty()) {
                        if(param.getType().contains("Filter")) {
                            supply.getDetails().put(detailKey, newValue);
                        }else {
                            try {
                                supply.getDetails().put(detailKey, Integer.parseInt(newValue));
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("유효하지 않은 supplyStatus: " + newValue, e);
                            }
                        }
                        }
                }else {
                    if (newValue != null && !newValue.isEmpty()) {
                        supply.getDetails().put(detailKey, newValue);
                    }
                }
            }
            return supply;
        }).onErrorMap(NumberFormatException.class, e -> new IllegalArgumentException("Invalid format for supplyAmount", e));
    }

    private Mono<Void> checkLimitsAndNotify(Supply supply, Group group) {
        boolean shouldNotify = isCareNeeded(supply);
        if (shouldNotify) {
            log.info("알림 전송");
            return deviceSupplyRepository.findFirstDeviceIdBySupplyId(supply.getId())
                    .flatMap(deviceId -> fcmService.sendMessageToGroup(
                            group.getId(),
                            SupplyType.getKoreanForType(supply.getType())+" 필요(" + group.getName() + ")",
                            group.getName() + "의 " + supply.getName() + "의 " +SupplyType.getKoreanForType(supply.getType())+"가 필요합니다."
                            , SupplyType.getActionForType(supply.getType()),
                            "DeviceDetailScreen",
                            deviceId)
                    ).then();
        } else {
            return Mono.empty();
        }
    }
}
