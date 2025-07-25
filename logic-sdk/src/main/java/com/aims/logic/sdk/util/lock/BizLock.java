package com.aims.logic.sdk.util.lock;

import java.util.List;

public interface BizLock {
    String buildKey(String logicId, String bizId);

    boolean isLocked(String key);

    boolean isBizLocked(String logicId, String bizId);

    boolean isStopping(String key);

    void setBizStopping(String key);

    boolean spinLock(String key);

//    void lock(String key) throws InterruptedException;

    void unlock(String key);

    List<String> getLockKeys();
}