package com.aims.logic.runtime.exception;

public class BizManuallyStoppedException extends RuntimeException {
    public BizManuallyStoppedException(String msg) {
        super(msg);
    }

    public BizManuallyStoppedException(String logicId, String bizId) {
        super(String.format("[%s]bizId:%s,主动停止实例运行，实例已中止", logicId, bizId));
    }
}
