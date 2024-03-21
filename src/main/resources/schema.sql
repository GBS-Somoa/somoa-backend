DROP TABLE IF EXISTS `Product`;
DROP TABLE IF EXISTS `Device_Supply`;
DROP TABLE IF EXISTS `Device`;
DROP TABLE IF EXISTS `Group_User`;
DROP TABLE IF EXISTS `User`;
DROP TABLE IF EXISTS `Order`;
DROP TABLE IF EXISTS `group`;


CREATE TABLE `Group` (
                         `group_id`    BIGINT    NOT NULL,
                         `group_name`    VARCHAR(100)    NOT NULL,
                         `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `Group_User` (
                              `user_id`    INT    NOT NULL,
                              `group_id`    BIGINT    NOT NULL,
                              `ordered_num`    INT    NOT NULL,
                              `role`    VARCHAR(30)    NOT NULL    COMMENT '관리자, 모든 권한, 기기 관리, 소모품 관리',
                              `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `Device` (
                          `device_id`    VARCHAR(36)    NOT NULL    COMMENT 'UUID',
                          `group_id`    BIGINT    NOT NULL,
                          `device_manufacturer`    VARCHAR(50)    NOT NULL,
                          `device_type`    VARCHAR(50)    NOT NULL,
                          `device_model`    VARCHAR(50)    NOT NULL,
                          `device_nickname`    VARCHAR(50)    NOT NULL,
                          `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `User` (
                        `user_id`    INT    NOT NULL ,
                        `user_username`    VARCHAR(30)    NOT NULL    COMMENT 'unique',
                        `user_password`    VARCHAR(100)    NOT NULL,
                        `user_nickname`    VARCHAR(16)    NOT NULL,
                        `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `Order` (
                         `order_id`    BIGINT    NOT NULL,
                         `group_id`    BIGINT    NOT NULL,
                         `supply_id`    INT    NOT NULL,
                         `order_status`    VARCHAR(30)    NOT NULL    COMMENT '주문완료, 배송중, 배송완료, 주문취소',
                         `product_name`    VARCHAR(500)    NOT NULL    COMMENT '실제 상품의 이름(ex. 다우니 프리미엄 실내건조)',
                         `order_store`    VARCHAR(500)    NOT NULL    COMMENT '쇼핑몰이름(ex. 싸팡, SSAG.COM)',
                         `order_store_id`    VARCHAR(500)    NOT NULL    COMMENT '쇼핑몰 측에서 넘겨주는 주문번호',
                         `product_img`    VARCHAR(500)    NULL    COMMENT '상품 이미지의 url',
                         `order_count`    INT    NOT NULL    COMMENT '구매 개수',
                         `order_amount`    VARCHAR(30)    NULL    COMMENT 'ex: 1.5L, 3L',
                         `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `Product` (
                           `product_id`    BIGINT    NOT NULL,
                           `product_name`    VARCHAR(500)    NOT NULL,
                           `product_amount`    VARCHAR(30)    NOT NULL    COMMENT '1.5L, 3L, 300g',
                           `product_barcode`    VARCHAR(100)    NULL,
                           `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `Device_Supply` (
                                 `device_id`    VARCHAR(36)    NOT NULL    COMMENT 'UUID',
                                 `supply_id`    VARCHAR(50)    NOT NULL    COMMENT 'Supply 컬렉션과 1:N',
                                 `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE `Group` ADD CONSTRAINT `PK_GROUP` PRIMARY KEY (
                                                           `group_id`
    );

ALTER TABLE `Device` ADD CONSTRAINT `PK_DEVICE` PRIMARY KEY (
                                                             `device_id`
    );

ALTER TABLE `User` ADD CONSTRAINT `PK_USER` PRIMARY KEY (
                                                         `user_id`
    );

ALTER TABLE `Order` ADD CONSTRAINT `PK_ORDER` PRIMARY KEY (
                                                           `order_id`
    );

ALTER TABLE `Product` ADD CONSTRAINT `PK_PRODUCT` PRIMARY KEY (
                                                               `product_id`
    );

ALTER TABLE `Group_User` ADD CONSTRAINT `FK_User_TO_Group_User_1` FOREIGN KEY (
                                                                               `user_id`
    )
    REFERENCES `User` (
                       `user_id`
        );

ALTER TABLE `Group_User` ADD CONSTRAINT `FK_Group_TO_Group_User_1` FOREIGN KEY (
                                                                                `group_id`
    )
    REFERENCES `Group` (
                        `group_id`
        );

ALTER TABLE `Device` ADD CONSTRAINT `FK_Group_TO_Device_1` FOREIGN KEY (
                                                                        `group_id`
    )
    REFERENCES `Group` (
                        `group_id`
        );

ALTER TABLE `Order` ADD CONSTRAINT `FK_Group_TO_Order_1` FOREIGN KEY (
                                                                      `group_id`
    )
    REFERENCES `Group` (
                        `group_id`
        );

ALTER TABLE `Device_Supply` ADD CONSTRAINT `FK_Device_TO_Device_Supply_1` FOREIGN KEY (
                                                                                       `device_id`
    )
    REFERENCES `Device` (
                         `device_id`
        );

ALTER TABLE `Group` MODIFY `group_id` BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE `User` MODIFY `user_id` INT NOT NULL AUTO_INCREMENT;
ALTER TABLE `Order` MODIFY `order_id` BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE `Product` MODIFY `product_id` BIGINT NOT NULL AUTO_INCREMENT;