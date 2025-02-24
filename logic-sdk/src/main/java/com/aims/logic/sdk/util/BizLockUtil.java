package com.aims.logic.sdk.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 根据字符串获取信号量加锁
 */
@Slf4j
public class BizLockUtil {
    /**
     * 过期时间
     */
    private static int EXPIRE_AFTER_MINUTE = 60;
    private static final int MAX_RETRY_TIMES = 100; // 最大自旋次数
    private static final long SPIN_WAIT_TIME = 100L; // 自旋等待时间（毫秒）
    // private static final ConcurrentMap<String, Semaphore> semaphores = new
    // ConcurrentHashMap<>();
    private static final Cache<String, ReentrantLock> lockCache = Caffeine.newBuilder().initialCapacity(200)
            // //最大容量为200
            // .maximumSize(200)
            .expireAfterAccess(Duration.ofMinutes(EXPIRE_AFTER_MINUTE))
            .build();

    /**
     * 自旋锁
     */
    public static boolean spinLock(String key) {
        log.debug("自旋锁开始获取 {}", key);
        ReentrantLock lock = lockCache.asMap().computeIfAbsent(key, k -> new ReentrantLock());

        int retryCount = 0;
        while (retryCount < MAX_RETRY_TIMES) {
            if (lock.tryLock()) {
                log.debug("获取锁成功, key: {}, 重试次数: {}", key, retryCount);
                return true;
            }
            retryCount++;
            try {
                Thread.sleep(SPIN_WAIT_TIME);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        log.warn("获取锁超时, key: {}, 重试次数: {}", key, retryCount);
        return false;
    }

    public static void lock(String key) throws InterruptedException {
        log.debug("begin lock {}", key);
        // Semaphore semaphore = semaphores.asMap().computeIfAbsent(key, k -> new
        // Semaphore(1));
        // semaphore.acquire();
        ReentrantLock lock = lockCache.asMap().computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
    }

    public static void unlock(String key) {
        log.debug("begin unlock {}", key);
        // Semaphore semaphore = semaphores.asMap().get(key);
        // if (semaphore != null) {
        // semaphore.release();
        // }
        ReentrantLock lock = lockCache.asMap().get(key);
        if (lock != null) {
            log.info("unlock ok,key:{}", key);
            lock.unlock();
        } else {
            log.info("unlock error，key缓存已过期，过期配置：{}分钟，,key {} ", EXPIRE_AFTER_MINUTE, key);
        }
    }

    public static List<String> getLockKeys() {
        return lockCache.asMap().keySet().stream().toList();
    }
}