package com.aims.logic.runtime.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 根据字符串获取信号量加锁
 */
@Slf4j
public class StringConcurrencyUtil {
    //    private static final ConcurrentMap<String, Semaphore> semaphores = new ConcurrentHashMap<>();
    private static final Cache<String, ReentrantLock> lockCache = Caffeine.newBuilder().
            initialCapacity(100)
            //    //最大容量为200
//     .maximumSize(200)
            .expireAfterAccess(Duration.ofMinutes(1))
            .build();

    public static void lock(String key) throws InterruptedException {
        log.info("ReentrantLock lock {}", key);
//        Semaphore semaphore = semaphores.asMap().computeIfAbsent(key, k -> new Semaphore(1));
//        semaphore.acquire();
        ReentrantLock lock = lockCache.asMap().computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
    }

    public static void unlock(String key) {
        log.info("unlock {}", key);
//        Semaphore semaphore = semaphores.asMap().get(key);
//        if (semaphore != null) {
//            semaphore.release();
//        }
        ReentrantLock lock = lockCache.asMap().computeIfAbsent(key, k -> new ReentrantLock());
        lock.unlock();
    }
}