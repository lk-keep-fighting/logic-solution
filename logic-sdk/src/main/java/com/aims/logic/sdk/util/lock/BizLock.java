package com.aims.logic.sdk.util.lock;

import java.util.List;

public interface BizLock {
    boolean spinLock(String key);
    void lock(String key) throws InterruptedException;
    void unlock(String key);
    List<String> getLockKeys();
}