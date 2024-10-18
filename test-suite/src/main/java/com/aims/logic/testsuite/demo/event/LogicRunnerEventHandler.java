package com.aims.logic.testsuite.demo.event;

import com.aims.logic.sdk.event.LogicRunnerEventListener;
import com.alibaba.fastjson2.JSON;
import org.springframework.stereotype.Component;

@Component
public class LogicRunnerEventHandler implements LogicRunnerEventListener {
    @Override
    public void onBizCompleted(String logicId, String bizId, Object returnData) {
        System.out.println("OnBizCompleted: logicId=" + logicId +
                ", bizId=" + bizId + ", returnData=" + JSON.toJSONString(returnData));
    }
}
