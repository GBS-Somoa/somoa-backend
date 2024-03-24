package com.somoa.serviceback.global.response;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

	private final T data;
	private final String message;
	private final long timestamp;
	private final int status;

	ApiResponse(T data, String message, int status) {
		this(data, message, System.currentTimeMillis(), status);
	}

	public static <T> ApiResponse<T> ok(T data, String message) {
		return new ApiResponse<>(data, message, HttpStatus.OK.value());
	}

	public static ApiResponse<Object> ok(String message) {
		return new ApiResponse<>(null, message, HttpStatus.OK.value());
	}

	public static ApiResponse<Object> error(String message, HttpStatus httpStatus) {
		return new ApiResponse<>(null, message, httpStatus.value());
	}
}
