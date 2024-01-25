package com.aims.logic.sdk.service;

import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.baomidou.mybatisplus.extension.service.IService;

public interface LogicInstanceService extends BaseService<LogicInstanceEntity> {
    LogicInstanceEntity getInstance(String logicId, String bizId);

    /**
     * 清理所有完成的实例
     * @param logicId 逻辑编号
     * @param bizId 业务标识
     * @return 清理实例行数
     */
//    long clearCompletedInstanceOver(String logicId, String bizId);

}
