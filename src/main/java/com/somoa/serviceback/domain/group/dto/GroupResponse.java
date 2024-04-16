package com.somoa.serviceback.domain.group.dto;

import lombok.Data;

@Data
public class GroupResponse {

	private Integer groupId;
	private String groupName;
	private int orderedNum;
	private Integer managerId;
	private String managerUsername;
	private String managerNickname;
}
