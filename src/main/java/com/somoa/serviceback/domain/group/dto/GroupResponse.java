package com.somoa.serviceback.domain.group.dto;

import com.somoa.serviceback.domain.group.entity.Group;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupResponse {

	private Integer groupId;
	private String groupName;

	public static GroupResponse of(Group group) {
		return GroupResponse.builder()
			.groupId(group.getId())
			.groupName(group.getName())
			.build();
	}
}
