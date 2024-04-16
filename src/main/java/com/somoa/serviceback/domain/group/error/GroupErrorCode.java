package com.somoa.serviceback.domain.group.error;

import org.springframework.http.HttpStatus;

import com.somoa.serviceback.global.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GroupErrorCode implements ErrorCode {

	GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 그룹이 존재하지 않습니다."),
	USER_NOT_IN_GROUP(HttpStatus.NOT_FOUND, "그룹에 등록되지 않은 유저입니다."),
	INVALID_GROUP_USER_ROLE(HttpStatus.BAD_REQUEST, "잘못된 그룹 유저 권한입니다."),
	INVALID_GROUP_OR_USER(HttpStatus.BAD_REQUEST, "유효하지 않는 그룹 번호이거나, 유저가 그룹 멤버가 아닙니다."),
	USER_ALREADY_IN_GROUP(HttpStatus.CONFLICT, "이미 그룹에 등록된 유저입니다."),
	NO_GROUP_MANAGEMENT_PERMISSION(HttpStatus.FORBIDDEN, "그룹 관리 권한이 없습니다."),
	GROUP_MANAGER_CANNOT_LEAVE(HttpStatus.FORBIDDEN, "관리자는 그룹에서 나갈 수 없습니다."),
	DOES_NOT_MATCH_NUMBER_OF_GROUPS(HttpStatus.BAD_REQUEST, "그룹 수가 일치하지 않습니다.")
	;

	private final HttpStatus httpStatus;
	private final String message;
}