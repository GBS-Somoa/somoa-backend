package com.somoa.serviceback.domain.group.repository.dto;

import lombok.Data;

@Data
public class GroupWithUserInfo {

	private Integer groupId;
	private String groupName;
	private Integer userId;
	private String role;
	private boolean alarm;
}
