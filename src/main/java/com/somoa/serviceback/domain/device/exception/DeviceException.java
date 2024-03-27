package com.somoa.serviceback.domain.device.exception;

import com.somoa.serviceback.global.error.ErrorCode;
import com.somoa.serviceback.global.exception.ApiException;

public class DeviceException extends ApiException {

	private String deviceId;

	public DeviceException(ErrorCode errorCode) {
		super(errorCode);
	}

	public DeviceException(ErrorCode errorCode, String deviceId) {
		super(errorCode);
		this.deviceId = deviceId;
	}
}
