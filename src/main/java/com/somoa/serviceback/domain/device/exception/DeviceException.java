package com.somoa.serviceback.domain.device.exception;

import com.somoa.serviceback.global.error.ErrorCode;
import com.somoa.serviceback.global.exception.ApiException;

public class DeviceException extends ApiException {

	public DeviceException(ErrorCode errorCode) {
		super(errorCode);
	}
}
