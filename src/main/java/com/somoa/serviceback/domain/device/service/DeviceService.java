package com.somoa.serviceback.domain.device.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.somoa.serviceback.domain.device.dto.DeviceRegisterParam;
import com.somoa.serviceback.domain.device.entity.Device;
import com.somoa.serviceback.domain.device.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    @Transactional
    public Mono<?> save(DeviceRegisterParam param) {
        // 제조사 서버 API 호출 : /api/device?device_id={device_id}
        // device_id : param.getCode();
        // API 호출의 응답으로 변경될 예정(현재는 dummy data)
        final String model = "모델 이름";
        final String type = "타입";
        final String manufacturer = "제조사";
        // ****************************************

        // response data
        Map<String, Object> data = new HashMap<>();
        data.put("deviceId", param.getCode());

        return deviceRepository.findById(param.getCode())
                .flatMap(existingDevice -> Mono.error(new IllegalArgumentException("이미 등록된 기기입니다.")))
                .switchIfEmpty(Mono.defer(() -> {
                            Device device = Device.builder()
                                    .id(param.getCode())
                                    .groupId(param.getGroupId())
                                    .nickname(param.getNickname())
                                    .model(model)
                                    .type(type)
                                    .manufacturer(manufacturer)
                                    .build();
                            return deviceRepository.saveForce(device)
                                .then(Mono.just(data));
                        }
                ));
    }
}
