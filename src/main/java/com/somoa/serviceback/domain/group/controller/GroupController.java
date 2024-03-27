package com.somoa.serviceback.domain.group.controller;

import com.somoa.serviceback.domain.group.dto.GroupModifyParam;
import com.somoa.serviceback.domain.group.dto.GroupRegisterParam;
import com.somoa.serviceback.domain.group.dto.GroupUserRegisterParam;
import com.somoa.serviceback.domain.group.dto.GroupUserRoleParam;
import com.somoa.serviceback.domain.group.service.GroupManagementService;
import com.somoa.serviceback.domain.group.service.GroupOrderService;
import com.somoa.serviceback.domain.group.service.GroupUserService;
import com.somoa.serviceback.global.annotation.Login;
import com.somoa.serviceback.global.handler.ResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/groups")
public class GroupController {

    private final GroupManagementService groupManagementService;
    private final GroupUserService groupUserService;
    private final GroupOrderService groupOrderService;

    @PostMapping
    public Mono<ResponseEntity<ResponseHandler>> create(@Login Integer loginUserId,
                                                        @RequestBody GroupRegisterParam param) {
        return groupManagementService.save(loginUserId, param)
            .flatMap(data -> ResponseHandler.ok(data, "그룹을 생성했습니다."));
    }

    @GetMapping
    public Mono<ResponseEntity<ResponseHandler>> list(@Login Integer loginUserId) {
        return groupManagementService.findAll(loginUserId)
            .collectList()
            .flatMap(data -> ResponseHandler.ok(data, "그룹 목록을 조회했습니다."));
    }

    @GetMapping("/{groupId}")
    public Mono<ResponseEntity<ResponseHandler>> detail(@Login Integer loginUserId,
                                                        @PathVariable("groupId") Integer groupId) {
        return groupManagementService.findOne(loginUserId, groupId)
            .flatMap(data -> ResponseHandler.ok(data, "그룹 상세 조회에 성공했습니다."));
    }

    @PatchMapping("/{groupId}")
    public Mono<ResponseEntity<ResponseHandler>> modify(@Login Integer loginUserId,
                                                        @PathVariable("groupId") Integer groupId,
        @RequestBody GroupModifyParam param) {
        return groupManagementService.modify(loginUserId, groupId, param)
            .then(ResponseHandler.ok("그룹 정보를 수정했습니다."));
    }

    @DeleteMapping("/{groupId}")
    public Mono<ResponseEntity<ResponseHandler>> delete(@Login Integer loginUserId,
                                                        @PathVariable("groupId") Integer groupId) {
        return groupManagementService.delete(loginUserId, groupId)
            .then(ResponseHandler.ok("그룹을 삭제했습니다."));
    }

    @DeleteMapping("/{groupId}/leave")
    public Mono<ResponseEntity<ResponseHandler>> leave(@Login Integer loginUserId,
                                                       @PathVariable("groupId") Integer groupId) {
        return groupUserService.leave(loginUserId, groupId)
            .then(ResponseHandler.ok("그룹에서 나갔습니다."));
    }

    @GetMapping("/{groupId}/users")
    public Mono<ResponseEntity<ResponseHandler>> getGroupMembers(@Login Integer loginUserId,
                                                                 @PathVariable("groupId") Integer groupId) {
        return groupUserService.getMembers(loginUserId, groupId)
            .flatMap(data -> ResponseHandler.ok(data, "그룹 멤버를 조회했습니다."));
    }

    @PostMapping("/{groupId}/users")
    public Mono<ResponseEntity<ResponseHandler>> addGroupMember(@Login Integer loginUserId,
                                                                @PathVariable("groupId") Integer groupId,
        @RequestBody GroupUserRegisterParam param) {
        return groupUserService.addMember(groupId, param)
            .then(ResponseHandler.ok("멤버를 추가했습니다."));
    }

    @PatchMapping("/{groupId}/users/{userId}/permission")
    public Mono<ResponseEntity<ResponseHandler>> modifyGroupMemberPermission(@Login Integer loginUserId,
                                                                             @PathVariable("groupId") Integer groupId,
                                                                             @PathVariable("userId") Integer userId,
                                                                             @RequestBody GroupUserRoleParam param) {
        return groupUserService.modifyMemberPermission(loginUserId, groupId, userId, param.getRole())
            .then(ResponseHandler.ok("멤버 권한을 수정했습니다."));
    }

    @GetMapping("/{groupId}/orders")
    public Mono<ResponseEntity<ResponseHandler>> getGroupOrders(@Login Integer loginUserId,
                                                                @PathVariable("groupId") Integer groupId) {
        return groupOrderService.getOrders(loginUserId, groupId)
            .flatMap(data -> ResponseHandler.ok(data, "그룹에 속한 주문 목록을 조회했습니다."));
    }
}
