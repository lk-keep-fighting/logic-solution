package com.aims.logic.sdk.util.lock;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "logic.biz-lock")
public class BizLockProperties {
    private String type = "memory"; // 默认使用内存锁
    private int expire = 30; // 锁失效时间，单位：秒

    public int getExpire() {
        if (expire == 0) {
            expire = 30;
        }
        return expire;
    }

    private SpinLock spinLock = new SpinLock();
    private Redis redis = new Redis();

    @Data
    public static class Redis {
        private String host;
        private int port = 6379;
        private String password;
        private int database = 0;
        private int timeout = 5000;
        private Cluster cluster;
    }

    @Data
    public static class Cluster {
        private String[] nodes;
    }

    @Data
    public static class SpinLock {
        private int retryTimes = 0;
        private long waitTime = 0L;
    }
}