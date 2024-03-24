package com.somoa.serviceback.domain.device.service;

import com.somoa.serviceback.domain.device.dto.DeviceExternalApiResponse;
import com.somoa.serviceback.domain.device.dto.DeviceRegisterParam;
import com.somoa.serviceback.domain.device.dto.DeviceResponse;
import com.somoa.serviceback.domain.device.dto.DeviceStatusDto;
import com.somoa.serviceback.domain.device.dto.DeviceUpdateParam;
import com.somoa.serviceback.domain.device.entity.Device;
import com.somoa.serviceback.domain.device.entity.DeviceType;
import com.somoa.serviceback.domain.device.exception.DeviceNotFoundException;
import com.somoa.serviceback.domain.device.repository.DeviceRepository;
import com.somoa.serviceback.domain.devicesupplies.repository.DeviceSuppliesRepository;
import com.somoa.serviceback.domain.supplies.entity.Supplies;
import com.somoa.serviceback.domain.supplies.repository.SuppliesRepository;
import com.somoa.serviceback.domain.group.entity.GroupUserRole;
import com.somoa.serviceback.domain.group.repository.GroupUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceSuppliesRepository deviceSuppliesRepository;
    private final SuppliesRepository suppliesRepository;
    private final DeviceRepository deviceRepository;
    private final GroupUserRepository groupUserRepository;

    private final String MANUFACTURER_SERVER_URL = "http://localhost:9090";
    private final String DEVICE_API_PATH = "/api/device";
    private final String DEVICE_ID_QUERY_PARAM = "device_id";

    @Transactional
    public Mono<Object> save(DeviceRegisterParam param) {
        // TODO: 제조사 서버 API 호출 : /api/device?device_id={device_id}
        // device_id : param.getCode();
        // API 호출의 응답으로 변경될 예정(현재는 dummy data)
        final String model = "모델 이름";
        final String type = DeviceType.WASHER;
        final String manufacturer = "제조사";

//        Mono<DeviceExternalApiResponse> responseMono = getDeviceResponse(param.getCode());
        Mono<DeviceExternalApiResponse> responseMono = Mono.just(DeviceExternalApiResponse.builder()
                .model(model)
                .type(type)
                .manufacturer(manufacturer)
                .build());
        // ****************************************

        // response data
        Map<String, Object> data = new HashMap<>();
        data.put("deviceId", param.getCode());

        return responseMono.flatMap(response -> deviceRepository.findById(param.getCode()))
                .flatMap(existingDevice -> Mono.error(new IllegalArgumentException("이미 등록된 기기입니다.")))
                .switchIfEmpty(responseMono.flatMap(response -> {
                            Device device = Device.builder()
                                    .id(param.getCode())
                                    .groupId(param.getGroupId())
                                    .nickname(param.getNickname())
                                    .model(response.getModel())
                                    .type(response.getType())
                                    .manufacturer(response.getManufacturer())
                                    .build();
                            return deviceRepository.saveForce(device)
                                .then(Mono.just(data));
                        }
                ));
    }

    public Mono<DeviceExternalApiResponse> getDeviceResponse(String deviceId) {
        WebClient webClient = WebClient.create(MANUFACTURER_SERVER_URL);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(DEVICE_API_PATH)
                        .queryParam(DEVICE_ID_QUERY_PARAM, deviceId)
                        .build())
                .retrieve()
                .bodyToMono(DeviceExternalApiResponse.class);
    }

    public Mono<DeviceResponse> findById(String deviceId) {
        return deviceRepository.findById(deviceId)
                .switchIfEmpty(Mono.error(new DeviceNotFoundException("기기를 찾을 수 없습니다 : " + deviceId)))
                .map(DeviceResponse::of);
    }

    public Mono<String> update(Integer userId, String deviceId, DeviceUpdateParam param) {
        return deviceRepository.findById(deviceId)
                .switchIfEmpty(Mono.error(new DeviceNotFoundException("기기를 찾을 수 없습니다 : " + deviceId)))
                .flatMap(device -> deviceRepository.findGroupByDeviceId(deviceId)
                        .flatMap(group -> groupUserRepository.findRole(group.getId(), userId)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("사용자에게 권한이 없습니다.")))
                                .flatMap(role -> {
                                    if (!role.equals(GroupUserRole.USER_ONLY_SUPPLY_MANAGE)) {
                                        device.setNickname(param.getDeviceName());
                                        return deviceRepository.save(device)
                                                .then(Mono.just("기기 이름이 성공적으로 수정되었습니다."));
                                    } else {
                                        return Mono.error(new IllegalArgumentException("사용자에게 권한이 없습니다."));
                                    }
                                })));
    }

    public Mono<String> delete(Integer userId, String deviceId) {
        return deviceRepository.findById(deviceId)
                .switchIfEmpty(Mono.error(new DeviceNotFoundException("기기를 찾을 수 없습니다 : " + deviceId)))
                .flatMap(device -> deviceRepository.findGroupByDeviceId(deviceId)
                        .flatMap(group -> groupUserRepository.findRole(group.getId(), userId)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("사용자에게 권한이 없습니다.")))
                                .flatMap(role -> {
                                    if (!role.equals(GroupUserRole.USER_ONLY_SUPPLY_MANAGE)) {
                                        return deviceRepository.delete(device)
                                                .then(Mono.just("기기가 성공적으로 삭제되었습니다."));
                                    } else {
                                        return Mono.error(new IllegalArgumentException("사용자에게 권한이 없습니다."));
                                    }
                                })));
    }

    public Mono<DeviceStatusDto> StatusUpdate(String deviceId, DeviceStatusDto deviceStatusDto) {
        // deviceId를 사용하여 DeviceSupplies에서 모든 suppliesId 찾기
        return deviceSuppliesRepository.findAllByDeviceId(deviceId)
                .flatMap(deviceSupplies ->
                        // 각 suppliesId에 대한 Supplies 정보 업데이트
                        suppliesRepository.findById(deviceSupplies.getSuppliesId())
                                .flatMap(supplies -> updateSupplies(supplies, deviceStatusDto))
                                .then(Mono.just(deviceSupplies))
                )
                .collectList()
                .flatMap(deviceSuppliesList -> Mono.just(deviceStatusDto));
    }

    private Mono<Supplies> updateSupplies(Supplies supplies, DeviceStatusDto deviceStatusDto) {
        deviceStatusDto.getSupplies().stream()
                .filter(supplyDto -> supplyDto.getSupplyType().equals(supplies.getType()))
                .findFirst()
                .ifPresent(supplyDto -> {
                    for (int i = 0; i < supplyDto.getDataProvided().size(); i++) {
                        String data = supplyDto.getDataProvided().get(i);
                        Object value = supplyDto.getValues().get(i);

                        switch (data) {
                            case "supplyStatus":
                                supplies.setStatus(value.toString());
                                break;
                            case "supplyChangeDate":
                                supplies.setChangeDate(value.toString());
                                break;
                            case "supplyAmount":
                                if (value instanceof Integer) {
                                    int newAmount = supplies.getAmount() - (Integer) value;
                                    supplies.setAmount(newAmount);

                                    /** amount가 limit 아래로 내려가면 알람 처리 로직  구현할것.
                                    if (supplies.getAmount() < supplies.getLimit()) {
                                        // 알람 로직
                                    }
                                     */
                                }
                                break;
                            // 추가적인 case 처리 가능
                        }
                    }
                });

        return suppliesRepository.save(supplies);
    }
}
