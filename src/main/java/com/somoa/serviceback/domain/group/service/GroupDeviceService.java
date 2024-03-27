package com.somoa.serviceback.domain.group.service;

import com.somoa.serviceback.domain.device.dto.DeviceResponse;
import com.somoa.serviceback.domain.device.exception.DeviceNotFoundException;
import com.somoa.serviceback.domain.device.repository.DeviceRepository;
import com.somoa.serviceback.domain.device.service.DeviceService;
import com.somoa.serviceback.domain.group.error.GroupErrorCode;
import com.somoa.serviceback.domain.group.exception.GroupException;
import com.somoa.serviceback.domain.group.repository.GroupRepository;
import com.somoa.serviceback.domain.group.repository.GroupUserRepository;
import com.somoa.serviceback.domain.supply.dto.SupplyResponse;
import com.somoa.serviceback.domain.supply.repository.DeviceSupplyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GroupDeviceService extends GroupBaseService {

    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;

    public GroupDeviceService(GroupRepository groupRepository, GroupUserRepository groupUserRepository, DeviceRepository deviceRepository, DeviceService deviceService) {
        super(groupRepository, groupUserRepository);
        this.deviceRepository = deviceRepository;
        this.deviceService = deviceService;
    }

    public Mono<List<DeviceResponse>> getDevices(Integer userId, Integer groupId) {
        return groupUserRepository.existsGroupUser(groupId, userId)
                .flatMap(userExists -> {
                    if (userExists) {
                        return deviceRepository.findAllByGroupId(groupId)
                                .flatMap(device -> deviceService.findById(device.getId()))
                                .collectList();
                    } else {
                        return Mono.error(new GroupException(GroupErrorCode.INVALID_GROUP_OR_USER));
                    }
                });
    }
}
