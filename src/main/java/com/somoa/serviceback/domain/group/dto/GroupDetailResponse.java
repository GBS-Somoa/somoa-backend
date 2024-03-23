package com.somoa.serviceback.domain.group.dto;

import java.util.List;

import com.somoa.serviceback.domain.group.entity.GroupUserRole;
import com.somoa.serviceback.domain.group.repository.dto.GroupWithUserInfo;
import com.somoa.serviceback.domain.user.dto.UserSimpleResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupDetailResponse {

	private Integer groupId;
	private String groupName;
	private Integer userId;
	private String role;
	private boolean alarm;
	private UserSimpleResponse manager;
	private List<UserSimpleResponse> members;

	public static GroupDetailResponse of(GroupWithUserInfo group, List<UserSimpleResponse> members) {
		return GroupDetailResponse.builder()
			.groupId(group.getGroupId())
			.groupName(group.getGroupName())
			.userId(group.getUserId())
			.role(group.getRole())
			.alarm(group.isAlarm())
			.manager(members.stream()
				.filter(member -> member.getRole().equals(GroupUserRole.MANAGER))
				.findFirst().orElse(null))
			.members(members)
			.build();
	}
}
