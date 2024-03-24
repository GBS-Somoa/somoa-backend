package com.somoa.serviceback.domain.group.dto;

import com.somoa.serviceback.domain.group.entity.Group;
import com.somoa.serviceback.domain.group.entity.GroupUser;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupDetailResponse {

	private Integer groupId;
	private String groupName;
	private String role;
	private boolean alarm;

	public static GroupDetailResponse of(Group group, GroupUser groupUser) {
		return GroupDetailResponse.builder()
			.groupId(group.getId())
			.groupName(group.getName())
			.role(groupUser.getRole())
			.alarm(groupUser.isAlarm())
			.build();
	}
}
