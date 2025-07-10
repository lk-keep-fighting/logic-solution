ALTER TABLE `logic_instance`
    ADD COLUMN `isAsync` tinyint(1) NULL DEFAULT 0 COMMENT '是否为异步调起' AFTER `retryTimes`;
ALTER TABLE `logic_bak`
    ADD INDEX `logicid_version_idx`(`id` ASC, `version` ASC) USING BTREE;