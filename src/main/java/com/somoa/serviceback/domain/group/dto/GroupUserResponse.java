package com.somoa.serviceback.domain.group.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupUserResponse {

	private Integer userId;
	private String userNickname;
	private String role;
}
