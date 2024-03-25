package com.somoa.serviceback.domain.group.exception;

import com.somoa.serviceback.global.error.ErrorCode;
import com.somoa.serviceback.global.exception.ApiException;

public class GroupException extends ApiException {

	public GroupException(ErrorCode errorCode) {
		super(errorCode);
	}
}
