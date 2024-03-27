package com.somoa.serviceback.domain.device.error;

import org.springframework.http.HttpStatus;

import com.somoa.serviceback.global.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeviceErrorCode implements ErrorCode {

	DEVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "기기를 찾을 수 없습니다."),
	DUPLICATE_DEVICE(HttpStatus.CONFLICT, "이미 등록된 기기입니다."),
	INVALID_DEVICE_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 소모품 타입입니다."),
	NO_DEVICE_MANAGEMENT_PERMISSION(HttpStatus.FORBIDDEN, "기기 관리 권한이 없습니다.")
	;

	private final HttpStatus httpStatus;
	private final String message;
}
