package com.somoa.serviceback.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSimpleResponse {

	private Integer userId;
	private String userNickname;
	private String role;
}
