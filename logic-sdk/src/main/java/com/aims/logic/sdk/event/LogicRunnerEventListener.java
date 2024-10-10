package com.aims.logic.sdk.event;

public interface LogicRunnerEventListener {
    void onBizCompleted(String logicId, String bizId, Object returnData);
}
