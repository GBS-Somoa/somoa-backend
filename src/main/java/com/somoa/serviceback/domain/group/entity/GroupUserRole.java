package com.somoa.serviceback.domain.group.entity;

public class GroupUserRole {

    public static final String MANAGER = "관리자";
    public static final String USER_ALL = "모든 권한";
    public static final String USER_ONLY_DEVICE_MANAGE = "기기 관리";
    public static final String USER_ONLY_SUPPLY_MANAGE = "소모품 관리";

    private static final String[] roles = {
        MANAGER, USER_ALL, USER_ONLY_DEVICE_MANAGE, USER_ONLY_SUPPLY_MANAGE
    };

    public static boolean isValidRole(String role) {
        for (String _role: roles) {
            if (role.equals(_role))
                return true;
        }
        return false;
    }
}
