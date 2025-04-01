ALTER TABLE `logic_instance`
    ADD COLUMN `isAsync` tinyint(1) NULL DEFAULT 0 COMMENT '是否为异步调起' AFTER `retryTimes`;