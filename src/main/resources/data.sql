INSERT INTO `User` (`user_id`, `user_username`, `user_password`, `user_nickname`, `created_at`, `updated_at`) VALUES
                                                                                                                  (1, 'user01', 'password01', 'nickname01', NOW(), NOW()),
                                                                                                                  (2, 'user02', 'password02', 'nickname02', NOW(), NOW()),
                                                                                                                  (3, 'user03', 'password03', 'nickname03', NOW(), NOW()),
                                                                                                                  (4, 'user04', 'password04', 'nickname04', NOW(), NOW()),
                                                                                                                  (5, 'user05', 'password05', 'nickname05', NOW(), NOW()),
                                                                                                                  (6, 'user06', 'password06', 'nickname06', NOW(), NOW()),
                                                                                                                  (7, 'user07', 'password07', 'nickname07', NOW(), NOW()),
                                                                                                                  (8, 'user08', 'password08', 'nickname08', NOW(), NOW()),
                                                                                                                  (9, 'user09', 'password09', 'nickname09', NOW(), NOW()),
                                                                                                                  (10, 'user10', 'password10', 'nickname10', NOW(), NOW());

INSERT INTO `Group` (`group_id`, `group_name`, `created_at`, `updated_at`) VALUES
                                                                               (1, 'Group 1', NOW(), NOW()),
                                                                               (2, 'Group 2', NOW(), NOW()),
                                                                               (3, 'Group 3', NOW(), NOW()),
                                                                               (4, 'Group 4', NOW(), NOW());

-- 1번 그룹에 유저 4명 추가
INSERT INTO `Group_User` (`user_id`, `group_id`, `ordered_num`, `role`, `updated_at`, `created_at`) VALUES
                                                                                                        (1, 1, 1, '관리자', NOW(), NOW()),
                                                                                                        (2, 1, 2, '모든 권한', NOW(), NOW()),
                                                                                                        (3, 1, 3, '기기 관리', NOW(), NOW()),
                                                                                                        (4, 1, 4, '소모품 관리', NOW(), NOW());

-- 2번 그룹에 유저 3명 추가
INSERT INTO `Group_User` (`user_id`, `group_id`, `ordered_num`, `role`, `updated_at`, `created_at`) VALUES
                                                                                                        (5, 2, 1, '관리자', NOW(), NOW()),
                                                                                                        (6, 2, 2, '모든 권한', NOW(), NOW()),
                                                                                                        (7, 2, 3, '기기 관리', NOW(), NOW());

-- 3번 그룹에 유저 2명 추가
INSERT INTO `Group_User` (`user_id`, `group_id`, `ordered_num`, `role`, `updated_at`, `created_at`) VALUES
                                                                                                        (8, 3, 1, '관리자', NOW(), NOW()),
                                                                                                        (9, 3, 2, '모든 권한', NOW(), NOW());

-- 4번 그룹에 유저 1명 추가
INSERT INTO `Group_User` (`user_id`, `group_id`, `ordered_num`, `role`, `updated_at`, `created_at`) VALUES
    (10, 4, 1, '관리자', NOW(), NOW());

-- Group 1에 5개의 Device 더미 데이터 추가
INSERT INTO `Device` (`device_id`, `group_id`, `device_manufacturer`, `device_type`, `device_model`, `device_nickname`, `created_at`, `updated_at`) VALUES
                                                                                                                                                        (UUID(), 1, 'Manufacturer A', 'refrigerator', 'Model A', 'Nickname A', NOW(), NOW()),
                                                                                                                                                        (UUID(), 1, 'Manufacturer BB', 'washer', 'Model BB', 'Nickname BB', NOW(), NOW()),
                                                                                                                                                        (UUID(), 1, 'Manufacturer B', 'washer', 'Model B', 'Nickname B', NOW(), NOW()),
                                                                                                                                                        (UUID(), 1, 'Manufacturer C', 'humidifier', 'Model C', 'Nickname C', NOW(), NOW()),
                                                                                                                                                        (UUID(), 1, 'Manufacturer D', 'dehumidifier', 'Model D', 'Nickname D', NOW(), NOW()),
                                                                                                                                                        (UUID(), 1, 'Manufacturer E', 'airPurifier', 'Model E', 'Nickname E', NOW(), NOW());

-- Group 2에 4개의 Device 더미 데이터 추가
INSERT INTO `Device` (`device_id`, `group_id`, `device_manufacturer`, `device_type`, `device_model`, `device_nickname`, `created_at`, `updated_at`) VALUES
                                                                                                                                                        (UUID(), 2, 'Manufacturer F', 'steamCloset', 'Model F', 'Nickname F', NOW(), NOW()),
                                                                                                                                                        (UUID(), 2, 'Manufacturer G', 'dishwasher', 'Model G', 'Nickname G', NOW(), NOW()),
                                                                                                                                                        (UUID(), 2, 'Manufacturer H', 'vaccumCleaner', 'Model H', 'Nickname H', NOW(), NOW()),
                                                                                                                                                        (UUID(), 2, 'Manufacturer I', 'waterPurifier', 'Model I', 'Nickname I', NOW(), NOW());

-- Group 3에 4개의 Device 더미 데이터 추가
INSERT INTO `Device` (`device_id`, `group_id`, `device_manufacturer`, `device_type`, `device_model`, `device_nickname`, `created_at`, `updated_at`) VALUES
                                                                                                                                                        (UUID(), 3, 'Manufacturer J', 'airConditioner', 'Model J', 'Nickname J', NOW(), NOW()),
                                                                                                                                                        (UUID(), 3, 'Manufacturer JJ', 'airConditioner', 'Model JJ', 'Nickname JJ', NOW(), NOW()),
                                                                                                                                                        (UUID(), 3, 'Manufacturer K', 'refrigerator', 'Model K', 'Nickname K', NOW(), NOW()),
                                                                                                                                                        (UUID(), 3, 'Manufacturer L', 'washer', 'Model L', 'Nickname L', NOW(), NOW()),
                                                                                                                                                        (UUID(), 3, 'Manufacturer M', 'humidifier', 'Model M', 'Nickname M', NOW(), NOW());

-- Group 4에 1개의 Device 더미 데이터 추가
INSERT INTO `Device` (`device_id`, `group_id`, `device_manufacturer`, `device_type`, `device_model`, `device_nickname`, `created_at`, `updated_at`) VALUES
    (UUID(), 4, 'Manufacturer N', 'dehumidifier', 'Model N', 'Nickname N', NOW(), NOW());

