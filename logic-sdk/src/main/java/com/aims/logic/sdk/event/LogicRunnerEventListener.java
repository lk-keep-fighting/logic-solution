package com.aims.logic.sdk.event;

public interface LogicRunnerEventListener {
    /**
     * 编排执行前通知，同步执行
     * @param logicId
     * @param bizId
     * @param params
     */
    void beforeLogicRun(String logicId, String bizId, Object params);

    /**
     * 编排执行停止后通知，同步执行
     * @param logicId
     * @param bizId
     * @param returnData
     */

    void afterLogicStop(String logicId, String bizId, Object returnData);

    /**
     * 编排实例执行完成通知，同步执行
     * @param logicId
     * @param bizId
     * @param returnData
     */

    void onBizCompleted(String logicId, String bizId, Object returnData);
}
