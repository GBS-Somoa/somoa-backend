package com.somoa.serviceback.domain.device.service;

import com.somoa.serviceback.domain.device.dto.DeviceRegisterParam;
import com.somoa.serviceback.domain.device.dto.DeviceExternalApiResponse;
import com.somoa.serviceback.domain.device.entity.Device;
import com.somoa.serviceback.domain.device.entity.DeviceType;
import com.somoa.serviceback.domain.device.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final String MANUFACTURER_SERVER_URL = "http://localhost:9090";
    private final String DEVICE_API_PATH = "/api/device";
    private final String DEVICE_ID_QUERY_PARAM = "device_id";

    @Transactional
    public Mono<Object> save(DeviceRegisterParam param) {
        // TODO: 제조사 서버 API 호출 : /api/device?device_id={device_id}
        // device_id : param.getCode();
        // API 호출의 응답으로 변경될 예정(현재는 dummy data)
        final String model = "모델 이름";
        final DeviceType type = DeviceType.WASHER;
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
}
