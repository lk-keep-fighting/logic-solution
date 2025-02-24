SET FOREIGN_KEY_CHECKS=0;

ALTER TABLE `logic_instance` ADD COLUMN `parentLogicId` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '父逻辑id' AFTER `bizId`;

ALTER TABLE `logic_instance` ADD COLUMN `parentBizId` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '父业务标识' AFTER `parentLogicId`;

ALTER TABLE `logic_instance` ADD COLUMN `isRunning` tinyint(1) NULL DEFAULT 0 COMMENT '是否运行中' AFTER `isOver`;

ALTER TABLE `logic_instance` ADD COLUMN `startTime` datetime(3) NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '开始时间' AFTER `serverTime`;

ALTER TABLE `logic_instance` ADD COLUMN `stopTime` datetime(3) NULL DEFAULT NULL COMMENT '结束时间' AFTER `startTime`;

ALTER TABLE `logic_instance` ADD COLUMN `duration` bigint NULL DEFAULT NULL COMMENT '持续时间' AFTER `stopTime`;

SET FOREIGN_KEY_CHECKS=1;