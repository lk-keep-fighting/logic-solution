package com.aims.logic.sdk.util.lock;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RFuture;
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
    private static final String LOCK_STOPPING_PREFIX = "biz_stopping:";


    public RedisBizLock(BizLockProperties properties) {
        this.properties = properties;
        this.spinLock = properties.getSpinLock();
        Config config = new Config();
        if (properties.getRedis().getHost() != null) {
            config.useSingleServer()
                    .setAddress("redis://" + properties.getRedis().getHost() + ":" + properties.getRedis().getPort())
                    .setPassword(properties.getRedis().getPassword())
                    .setDatabase(properties.getRedis().getDatabase());
        } else if (properties.getRedis().getCluster() != null) {
            String[] nodes = properties.getRedis().getCluster().getNodes();
            for (int i = 0; i < nodes.length; i++) {
                if (!nodes[i].startsWith("redis://") && !nodes[i].startsWith("rediss://")) {
                    nodes[i] = "redis://" + nodes[i];
                }
            }
            config.useClusterServers()
                    .addNodeAddress(nodes)
                    .setPassword(properties.getRedis().getPassword());
        }

        this.redisson = Redisson.create(config);
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
    public boolean isBizLocked(String logicId, String bizId) {
        return isLocked(logicId + ":" + bizId);
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


    @Override
    public void unlock(String key) {
        String lockKey = LOCK_PREFIX + key;
        String stoppingBizKey = LOCK_STOPPING_PREFIX + key;
        RLock lock = redisson.getLock(lockKey);

        try {
            if (lock.isHeldByCurrentThread()) {
                try {
                    // 先解锁再删除key，避免竞争条件
                    lock.unlock();
                    log.debug("Unlock successful for key: {}", key);

                    // 异步删除key避免阻塞（根据业务需求选择同步/异步）
                    RFuture<Long> delLockFuture = redisson.getKeys().deleteAsync(lockKey);
                    RFuture<Long> delStopFuture = redisson.getKeys().deleteAsync(stoppingBizKey);

                    // 等待删除操作完成（可选）
                    delLockFuture.await();
                    delStopFuture.await();

                    if (delLockFuture.isSuccess() && delStopFuture.isSuccess()) {
                        log.debug("Cleanup completed for key: {}", key);
                    }
                } catch (Exception e) {
                    log.error("Failed to unlock or cleanup for key: {}", key, e);
                    // 考虑重试机制或告警
                }
            } else {
                log.warn("Unlock failed: Lock either expired or not held by current thread. Key: {}", key);
                // 可添加监控指标
            }
        } finally {
            // 确保锁资源释放
            if (lock.isHeldByCurrentThread()) {
                try {
                    lock.unlock();
                } catch (IllegalMonitorStateException e) {
                    log.debug("Lock already released for key: {}", key);
                }
            }
        }
    }

    @Override
    public List<String> getLockKeys() {
        return redisson.getKeys().getKeysStreamByPattern(LOCK_PREFIX + "*")
                .map(key -> key.substring(LOCK_PREFIX.length()))
                .toList();
    }
}