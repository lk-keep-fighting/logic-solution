package com.aims.logic.sdk.util.lock;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
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

    public RedisBizLock(BizLockProperties properties) {
        this.properties = properties;
        this.spinLock = properties.getSpinLock();
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + properties.getRedis().getHost() + ":" + properties.getRedis().getPort())
                .setPassword(properties.getRedis().getPassword())
                .setDatabase(properties.getRedis().getDatabase());
        this.redisson = Redisson.create(config);
    }

    @Override
    public boolean isLocked(String key) {
        String lockKey = LOCK_PREFIX + key;
        RLock lock = redisson.getLock(lockKey);
        return lock.isLocked();
    }

    @Override
    public boolean spinLock(String key) {
        String lockKey = LOCK_PREFIX + key;
        RLock lock = redisson.getLock(lockKey);

        int retryCount = 0;
        while (retryCount <= spinLock.getRetryTimes()) {
            try {
                if (lock.tryLock(0, properties.getRedis().getExpire(), TimeUnit.SECONDS)) {
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

    @Override
    public void lock(String key) throws InterruptedException {
        String lockKey = LOCK_PREFIX + key;
        RLock lock = redisson.getLock(lockKey);
        lock.lock(properties.getRedis().getExpire(), TimeUnit.SECONDS);
    }

    @Override
    public void unlock(String key) {
        String lockKey = LOCK_PREFIX + key;
        RLock lock = redisson.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.info("unlock ok,key:{}", key);
        } else {
            log.info("unlock error，当前线程未持有锁或锁已过期, key:{}", key);
        }
    }

    @Override
    public List<String> getLockKeys() {
        return redisson.getKeys().getKeysStreamByPattern(LOCK_PREFIX + "*")
                .map(key -> key.substring(LOCK_PREFIX.length()))
                .toList();
    }
}