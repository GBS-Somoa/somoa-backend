package com.somoa.serviceback.domain.group.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.somoa.serviceback.domain.group.dto.GroupRegisterParam;
import com.somoa.serviceback.domain.group.dto.GroupResponse;
import com.somoa.serviceback.domain.group.service.GroupService;
import com.somoa.serviceback.global.handler.ResponseHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;
    private static final Integer userId = 1;  // 임시, TODO: Token에서 userId 정보 추출

    @PostMapping
    public Mono<ResponseEntity<ResponseHandler>> create(@RequestBody GroupRegisterParam param) {
        return groupService.save(userId, param)
                .flatMap(data -> ResponseHandler.ok(data, "장소를 생성했습니다."))
                .onErrorResume(error -> {
                    log.error("error occurs!!", error);
                    return ResponseHandler.error("internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @GetMapping
    public Mono<ResponseEntity<ResponseHandler>> list() {
        return groupService.findAll(userId)
            .collectList()
            .flatMap(data -> ResponseHandler.ok(data, "장소 목록을 조회했습니다."));
    }

    @GetMapping("/{groupId}")
    public Mono<ResponseEntity<ResponseHandler>> detail(@PathVariable("groupId") Integer groupId) {
        return groupService.findOne(userId, groupId)
            .flatMap(data -> ResponseHandler.ok(data, "장소 상세 조회에 성공했습니다."))
            .onErrorResume(error -> {
                log.error("error occurs!!", error);
                return ResponseHandler.error("internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
            });
    }
}
