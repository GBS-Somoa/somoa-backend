package com.somoa.serviceback.domain.device.service;

import com.somoa.serviceback.domain.device.dto.*;
import com.somoa.serviceback.domain.device.entity.Device;
import com.somoa.serviceback.domain.device.exception.DeviceNotFoundException;
import com.somoa.serviceback.domain.device.repository.DeviceRepository;
import com.somoa.serviceback.domain.group.entity.GroupUserRole;
import com.somoa.serviceback.domain.group.repository.GroupUserRepository;
import com.somoa.serviceback.domain.supply.dto.SupplyRegisterParam;
import com.somoa.serviceback.domain.supply.dto.SupplyResponse;
import com.somoa.serviceback.domain.supply.entity.*;
import com.somoa.serviceback.domain.supply.repository.DeviceSupplyRepository;
import com.somoa.serviceback.domain.supply.repository.GroupSupplyRepository;
import com.somoa.serviceback.domain.supply.repository.SupplyRepository;
import com.somoa.serviceback.global.config.PropertiesConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final GroupUserRepository groupUserRepository;
    private final SupplyRepository supplyRepository;
    private final DeviceSupplyRepository deviceSupplyRepository;
    private final GroupSupplyRepository groupSupplyRepository;
    private final PropertiesConfig propertiesConfig;
    private final String DEVICE_API_PATH = "/api/device";
    private final String DEVICE_ID_QUERY_PARAM = "device_id";

    @Transactional
    public Mono<Map<String, Object>> save(DeviceRegisterParam param) {

        Mono<DeviceApiResponse> responseMono = getDeviceResponse(param.getCode());

        // response data
        Map<String, Object> data = new HashMap<>();
        data.put("deviceId", param.getCode());

        return deviceRepository.findById(param.getCode())
                .flatMap(existingDevice -> Mono.error(new IllegalArgumentException("이미 등록된 기기입니다.")))
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
            return Mono.error(new IllegalArgumentException("유효하지 않은 소모품 타입입니다 : " + param.getType()));
        }

        Map<String, Object> details = new HashMap<>();
        Map<String, Object> supplyLimit = new HashMap<>();
        for (String detail : param.getDetails()) {
            switch (detail) {
                case "supplyAmount":
                    details.put("supplyAmount", 0);
                    supplyLimit.put("supplyAmount", 0); // 기본값 0 -> 알람안뜨게설정
                    break;
                case "supplyStatus":
                    details.put("supplyStatus", "good"); // 기본값 "good"
                    supplyLimit.put("supplyStatus", "null"); // 기본값 "null" -> 알람안뜨게설정
                    break;
                case "supplyChangeDate":
                    details.put("supplyChangeDate",Instant.now());
                    supplyLimit.put("supplyChangeDate", 365); // 기본값 365일
                    break;
            }
        }

        Supply newSupply = Supply.builder()
                .type(param.getType())
                .name(param.getName())
                .details(details) // 기존 디테일 셋팅 로직을 따름
                .supplyLimit(supplyLimit) // 새로 추가된 부분
                .build();

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
                .switchIfEmpty(Mono.error(new DeviceNotFoundException("기기를 찾을 수 없습니다 : " + deviceId)))
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
                .switchIfEmpty(Mono.error(new DeviceNotFoundException("기기를 찾을 수 없습니다 : " + deviceId)))
                .flatMap(device -> deviceRepository.findGroupByDeviceId(deviceId)
                        .flatMap(group -> groupUserRepository.findGroupUser(group.getId(), userId)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("사용자에게 권한이 없습니다.")))
                                .flatMap(groupUser -> {
                                    if (!groupUser.getRole().equals(GroupUserRole.USER_ONLY_SUPPLY_MANAGE)) {
                                        device.setNickname(param.getDeviceName());
                                        return deviceRepository.save(device)
                                                .then(Mono.just(deviceId));
                                    } else {
                                        return Mono.error(new IllegalArgumentException("사용자에게 권한이 없습니다."));
                                    }
                                })));
    }

    public Mono<String> delete(Integer userId, String deviceId) {
        return deviceRepository.findById(deviceId)
                .switchIfEmpty(Mono.error(new DeviceNotFoundException("기기를 찾을 수 없습니다 : " + deviceId)))
                .flatMap(device -> deviceRepository.findGroupByDeviceId(deviceId)
                        .flatMap(group -> groupUserRepository.findGroupUser(group.getId(), userId)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("사용자에게 권한이 없습니다.")))
                                .flatMap(groupUser -> {
                                    if (!groupUser.getRole().equals(GroupUserRole.USER_ONLY_SUPPLY_MANAGE)) {
                                        return deleteDeviceSupply(device.getId())
                                                .then(deviceRepository.delete(device)
                                                        .then(Mono.just(deviceId)));

                                    } else {
                                        return Mono.error(new IllegalArgumentException("사용자에게 권한이 없습니다."));
                                    }
                                })));
    }

    private Mono<Void> deleteDeviceSupply(String deviceId) {
        return deviceSupplyRepository.deleteByDeviceId(deviceId)
                .then(Mono.empty());
    }

    /**
     * Todo: 기기-테스트앱 연결해서 진행 + fcm groupId기반 메서드 제대로 생성 후 작업
     * @param deviceId
     * @param deviceStatusDto
     * @return

    public Mono<DeviceStatusDto> StatusUpdate(String deviceId, DeviceStatusDto deviceStatusDto) {
        // deviceId를 사용하여 DeviceSupplies에서 모든 suppliesId 찾기
        return deviceSupplyRepository.findAllByDeviceId(deviceId)
                //.flatMap(deviceSupplies ->
                        // 각 suppliesId에 대한 Supplies 정보 업데이트
                        //suppliesRepository.findById(deviceSupplies.getSuppliesId())
                                //.flatMap(supplies -> updateSupplies(supplies, deviceStatusDto))
                           //     .then(Mono.just(deviceSupplies))
               // )
                .collectList()
                .flatMap(deviceSuppliesList -> Mono.just(deviceStatusDto));
    }
     */
}
