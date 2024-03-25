package com.somoa.serviceback.global.exception;

import com.somoa.serviceback.global.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApiException extends RuntimeException {

	private final ErrorCode errorCode;
}
