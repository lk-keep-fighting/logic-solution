package com.aims.logic.sdk.util.lock;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
@Scope("singleton")
@ConditionalOnProperty(prefix = "logic.biz-lock", name = "type", havingValue = "MEMORY", matchIfMissing = true)
public class MemoryBizLock implements BizLock {
    private final Cache<String, ReentrantLock> lockCache;
    private final Cache<String, Integer> stoppingBizCache;
    private final BizLockProperties.SpinLock spinLock;

    public MemoryBizLock(BizLockProperties properties) {
        this.spinLock = properties.getSpinLock();
        this.lockCache = Caffeine.newBuilder()
                .initialCapacity(200)
                .expireAfterAccess(Duration.ofMinutes(60))
                .build();
        this.stoppingBizCache = Caffeine.newBuilder()
                .initialCapacity(20)
                .expireAfterAccess(Duration.ofMinutes(60))
                .build();
    }

    @Override
    public String buildKey(String logicId, String bizId) {
        return logicId + ":" + bizId;
    }

    @Override
    public boolean isLocked(String key) {
        return lockCache.asMap().containsKey(key);
    }

    @Override
    public boolean isStopping(String key) {
        return stoppingBizCache.asMap().containsKey(key);
    }

    @Override
    public void setBizStopping(String key) {
        if (isLocked(key))
            stoppingBizCache.put(key, 0);
        else
            throw new RuntimeException("指定的实例不在运行中，无法停止。");
    }

    @Override
    public boolean spinLock(String key) {
        log.debug("自旋锁开始获取 {}", key);
        ReentrantLock lock = lockCache.asMap().computeIfAbsent(key, k -> new ReentrantLock());

        int retryCount = 0;
        while (retryCount <= spinLock.getRetryTimes()) {
            if (lock.tryLock()) {
                log.info("获取锁成功, key: {}, 重试次数: {}", key, retryCount);
                return true;
            }
            retryCount++;
            try {
                Thread.sleep(spinLock.getWaitTime());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        log.error("获取锁超时, key: {}, 重试次数: {}", key, retryCount);
        return false;
    }

    @Override
    public void lock(String key) throws InterruptedException {
        log.debug("begin lock {}", key);
        ReentrantLock lock = lockCache.asMap().computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
    }

    @Override
    public void unlock(String key) {
        log.debug("begin unlock {}", key);
        ReentrantLock lock = lockCache.asMap().get(key);
        if (lock != null) {
            lockCache.asMap().remove(key);
            log.info("remove key ok:{}", key);
            stoppingBizCache.asMap().remove(key);
            lock.unlock();
            log.info("unlock key ok:{}", key);
        } else {
            log.error("unlock error，key不存在或已过期，过期配置：60分钟，key {} ", key);
        }

    }

    @Override
    public List<String> getLockKeys() {
        return lockCache.asMap().keySet().stream().toList();
    }
}