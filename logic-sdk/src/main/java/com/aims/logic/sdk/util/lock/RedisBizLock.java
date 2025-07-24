package com.aims.logic.sdk.util.lock;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "logic.biz-lock", name = "type", havingValue = "REDIS")
public class RedisBizLock implements BizLock {
    private final RedissonClient redisson;
    private final BizLockProperties properties;
    private final BizLockProperties.SpinLock spinLock;
    private static final String LOCK_PREFIX = "biz_lock:";
    private static final String LOCK_STOPPING_PREFIX = "biz_stopping:";

    // 注入Spring管理的RedissonClient和BizLockProperties
    @Autowired
    public RedisBizLock(BizLockProperties properties, RedissonClient redisson) {
        this.properties = properties;
        this.spinLock = properties.getSpinLock();
        this.redisson = redisson; // 使用Spring注入的RedissonClient
    }

    @Override
    public String buildKey(String logicId, String bizId) {
        return logicId + ":" + bizId;
    }

    @Override
    public boolean isLocked(String key) {
        String lockKey = LOCK_PREFIX + key;
        RLock lock = redisson.getLock(lockKey);
        return lock.isLocked();
    }

    @Override
    public boolean isStopping(String key) {
        return redisson.getBucket(LOCK_STOPPING_PREFIX + key).isExists();
    }

    @Override
    public void setBizStopping(String key) {
        if (isLocked(key))
            redisson.getBucket(LOCK_STOPPING_PREFIX + key).set(0);
        else
            throw new RuntimeException("指定的实例不在运行中，无法停止。");
    }

    @Override
    public boolean spinLock(String key) {
        String lockKey = LOCK_PREFIX + key;
        RLock lock = redisson.getLock(lockKey);

        int retryCount = 0;
        while (retryCount <= spinLock.getRetryTimes()) {
            try {
                if (lock.tryLock(0, properties.getExpire(), TimeUnit.SECONDS)) {
                    log.debug("获取锁成功, key: {},重试次数: {}", key, retryCount);
                    return true;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("获取锁抛出异常InterruptedException, key: {}, 重试次数: {}", key, retryCount);
                e.printStackTrace();
                return false;
            }
            retryCount++;
            try {
                Thread.sleep(spinLock.getWaitTime());
            } catch (InterruptedException e) {
                log.error("获取锁抛出异常InterruptedException, key: {}, 重试次数: {}", key, retryCount);
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return false;
            }
        }
        log.warn("获取锁超时, key: {}, 重试次数: {}", key, retryCount);
        return false;
    }

//    @Override
//    public void lock(String key) throws InterruptedException {
//        String lockKey = LOCK_PREFIX + key;
//        RLock lock = redisson.getLock(lockKey);
//        lock.lock(properties.getRedis().getExpire(), TimeUnit.SECONDS);
//    }

    @Override
    public void unlock(String key) {
        String lockKey = LOCK_PREFIX + key;
        String stoppingBizKey = LOCK_STOPPING_PREFIX + key;
        RLock lock = redisson.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            redisson.getKeys().delete(lockKey);
            log.info("delete key ok:{}", key);
            redisson.getKeys().delete(stoppingBizKey);
            lock.unlock();
            log.info("unlock key ok:{}", key);
        } else {
            log.info("unlock error，redis不存在锁，锁已被删除或已过期, key:{}", key);
        }
    }

    @Override
    public List<String> getLockKeys() {
        return redisson.getKeys().getKeysStreamByPattern(LOCK_PREFIX + "*")
                .map(key -> key.substring(LOCK_PREFIX.length()))
                .toList();
    }
}