package com.aims.logic.runtime.custom;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

// 自定义拒绝策略,主要用来记录日志
@Slf4j
public class CustomDiscardOldestPolicy implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        // 获取当前时间
        long currentTime = System.currentTimeMillis();
        // 记录被丢弃任务的名称
        String taskName = r.toString();
        log.warn("添加日志线程池任务队列已满，日志被丢弃，[" + currentTime + "] Task " + taskName + " is discarded.");
        // 记录日志
        // 如果线程池没有被关闭
        if (!executor.isShutdown()) {
            // 获取任务队列
            BlockingQueue<Runnable> queue = executor.getQueue();
            // 获取队列的第一个任务（最旧的任务）
            Runnable oldestTask = queue.poll();
            if (oldestTask != null) {
                // 将新的任务添加到任务队列中
                executor.execute(r);
            } else {
                // 如果队列是空的，记录日志并拒绝任务
                log.warn("添加日志异常，[" + currentTime + "] Task " + taskName + " is rejected because the queue is empty.");
            }
        }
    }
}