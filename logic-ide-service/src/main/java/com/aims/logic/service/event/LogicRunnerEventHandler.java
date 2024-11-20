package com.aims.logic.service.event;

import com.aims.logic.runtime.contract.dsl.ParamTreeNode;

public class LogicRunnerEventHandler {
    public void process(ParamTreeNode event) {
        System.out.println("LogicRunnerEventHandler.process: " + event.getName());
        switch (event.getName()) {
            case "OnBizCompleted":
                // do something
                break;
            case "OnBizError":
                // do something
                break;
            case "OnBizStart":
                // do something
                break;
            default:
                break;
        }
    }
}
