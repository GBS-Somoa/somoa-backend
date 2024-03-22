package com.somoa.serviceback.domain.device.service;

import com.somoa.serviceback.domain.device.dto.DeviceRegisterParam;
import com.somoa.serviceback.domain.device.dto.DeviceResponse;
import com.somoa.serviceback.domain.device.entity.Device;
import com.somoa.serviceback.domain.device.repository.DeviceRepository;
import com.somoa.serviceback.global.handler.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final String MANUFACTURER_SERVER_URL = "http://localhost:9090";
    private final String DEVICE_API_PATH = "/api/device";
    private final String DEVICE_ID_QUERY_PARAM = "device_id";

    @Transactional
    public Mono<?> save(DeviceRegisterParam param) {
        // 제조사 서버 API 호출 : /api/device?device_id={device_id}
        // device_id : param.getCode();
        // API 호출의 응답으로 변경될 예정(현재는 dummy data)
        final String model = "모델 이름";
        final String type = "타입";
        final String manufacturer = "제조사";

        // Mono<DeviceResponse> responseMono = getDeviceResponse(param.getCode());
            Mono<DeviceResponse> responseMono = Mono.just(DeviceResponse.builder()
                    .model(model)
                    .type(type)
                    .manufacturer(manufacturer)
                    .build());
        // ****************************************

        return responseMono.flatMap(response -> deviceRepository.findByCode(param.getCode()))
                .flatMap(existingDevice -> Mono.error(new IllegalArgumentException("이미 등록된 기기입니다.")))
                .switchIfEmpty(responseMono.flatMap(response -> {
                            Device device = Device.builder()
                                    .code(param.getCode())
                                    .groupId(param.getGroupId())
                                    .nickname(param.getNickname())
                                    .model(response.getModel())
                                    .type(response.getType())
                                    .manufacturer(response.getManufacturer())
                                    .build();
                            return deviceRepository.save(device)
                                    .map(savedDevice -> Mono.empty());
                        }
                ));
    }

    public Mono<DeviceResponse> getDeviceResponse(String deviceId) {
        WebClient webClient = WebClient.create(MANUFACTURER_SERVER_URL);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(DEVICE_API_PATH)
                        .queryParam(DEVICE_ID_QUERY_PARAM, deviceId)
                        .build())
                .retrieve()
                .bodyToMono(DeviceResponse.class);
    }
}
