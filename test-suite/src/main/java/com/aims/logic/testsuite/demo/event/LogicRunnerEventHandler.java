package com.aims.logic.testsuite.demo.event;

import com.aims.logic.sdk.event.LogicRunnerEventListener;
import com.alibaba.fastjson2.JSON;
import org.springframework.stereotype.Component;

@Component
public class LogicRunnerEventHandler implements LogicRunnerEventListener {

    @Override
    public void beforeLogicRun(String logicId, String bizId, Object params) {
        System.out.println("beforeLogicRun: logicId=" + logicId +
                ", bizId=" + bizId + ", params=" + JSON.toJSONString(params));
    }

    @Override
    public void afterLogicStop(String logicId, String bizId, Object returnData) {
        System.out.println("afterLogicStop: logicId=" + logicId +
                ", bizId=" + bizId + ", returnData=" + JSON.toJSONString(returnData));
    }

    @Override
    public void onBizCompleted(String logicId, String bizId, Object returnData) {
        System.out.println("onBizCompleted: logicId=" + logicId +
                ", bizId=" + bizId + ", returnData=" + JSON.toJSONString(returnData));
    }
}
