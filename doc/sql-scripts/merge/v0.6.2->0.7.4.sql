ALTER TABLE `logic_instance`
ADD COLUMN `createTime` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '实例创建时间' AFTER `isRunning`,
ADD COLUMN `retryTimes` int NULL DEFAULT 0 COMMENT '重试次数' AFTER `duration`;