package com.somoa.serviceback.domain.group.entity;

public class GroupUserRole {

    public static final String MANAGER = "관리자";
    public static final String USER_ALL = "모든 권한";
    public static final String USER_ONLY_SUPPLY_MANAGE = "소모품 관리";

    private static final String[] userRoles = {
        USER_ALL, USER_ONLY_SUPPLY_MANAGE
    };

    public static boolean isValidRole(String role) {
        for (String userRole: userRoles) {
            if (role.equals(userRole))
                return true;
        }
        return false;
    }
}
