package com.somoa.serviceback.domain.group.controller;

import com.somoa.serviceback.domain.group.dto.GroupRegisterParam;
import com.somoa.serviceback.domain.group.entity.Group;
import com.somoa.serviceback.domain.group.service.GroupService;
import com.somoa.serviceback.global.handler.ResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    private Mono<ResponseEntity<ResponseHandler>> create(@RequestBody GroupRegisterParam param) {
        Integer userId = 1;     // Token에서 userId 정보 추출
        return groupService.save(userId, param)
                .flatMap(data -> ResponseHandler.ok(data, "장소를 생성했습니다."))
                .onErrorResume(error -> {
                    log.error("error occurs!!", error);
                    return ResponseHandler.error("internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }
}
