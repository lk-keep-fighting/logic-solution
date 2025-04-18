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
    private final BizLockProperties.SpinLock spinLock;

    public MemoryBizLock(BizLockProperties properties) {
        this.spinLock = properties.getSpinLock();
        this.lockCache = Caffeine.newBuilder()
                .initialCapacity(200)
                .expireAfterAccess(Duration.ofMinutes(60))
                .build();
    }

    @Override
    public boolean isLocked(String key) {
        return lockCache.asMap().containsKey(key);
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
            log.info("unlock ok,key:{}", key);
            lock.unlock();
        } else {
            log.error("unlock error，key缓存已过期，过期配置：60分钟，key {} ", key);
        }
    }

    @Override
    public List<String> getLockKeys() {
        return lockCache.asMap().keySet().stream().toList();
    }
}