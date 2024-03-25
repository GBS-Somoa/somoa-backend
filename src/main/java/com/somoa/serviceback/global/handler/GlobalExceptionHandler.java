package com.somoa.serviceback.global.handler;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;

import com.somoa.serviceback.global.error.CommonErrorCode;
import com.somoa.serviceback.global.error.ErrorCode;
import com.somoa.serviceback.global.exception.ApiException;
import com.somoa.serviceback.global.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	// 커스텀 예외 처리
	@ExceptionHandler(ApiException.class)
	public Mono<ResponseEntity<Object>> handleCustomException(ApiException e) {
		ErrorCode errorCode = e.getErrorCode();
		return handleExceptionInternal(errorCode);
	}

	// 데이터베이스 관련 예외 처리
	@ExceptionHandler(DataAccessException.class)
	public Mono<ResponseEntity<Object>> handleDataAccessException(DataAccessException e) {
		log.warn("dataAccessException: ", e);
		ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
		return handleExceptionInternal(errorCode, e.getMessage());
	}

	private Mono<ResponseEntity<Object>> handleExceptionInternal(ErrorCode errorCode) {
		return Mono.just(ResponseEntity.status(errorCode.getHttpStatus())
			.body(makeErrorResponse(errorCode)));
	}

	private Mono<ResponseEntity<Object>> handleExceptionInternal(ErrorCode errorCode, String message) {
		return Mono.just(ResponseEntity.status(errorCode.getHttpStatus())
			.body(makeErrorResponse(errorCode, message)));
	}

	private ApiResponse<Object> makeErrorResponse(ErrorCode errorCode) {
		return ApiResponse.error(errorCode.getMessage(), errorCode.getHttpStatus());
	}

	private ApiResponse<Object> makeErrorResponse(ErrorCode errorCode, String message) {
		return ApiResponse.error(message, errorCode.getHttpStatus());
	}
}
