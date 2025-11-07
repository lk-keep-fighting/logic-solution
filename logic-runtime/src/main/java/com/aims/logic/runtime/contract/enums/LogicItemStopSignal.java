package com.aims.logic.runtime.contract.enums;

public enum LogicItemStopSignal {
    errorInterrupt(-1),
    def(0),//默认不需要停止
    subLogicWaitForContinue(1);
    final int value;
    LogicItemStopSignal(int value) {
        this.value = value;
    }

}
