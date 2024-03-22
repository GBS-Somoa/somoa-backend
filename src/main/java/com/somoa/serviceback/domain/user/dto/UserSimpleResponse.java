package com.somoa.serviceback.domain.user.dto;

import lombok.Data;

@Data
public class UserSimpleResponse {

	private Integer id;
	private String userNickname;
	private String role;
}
