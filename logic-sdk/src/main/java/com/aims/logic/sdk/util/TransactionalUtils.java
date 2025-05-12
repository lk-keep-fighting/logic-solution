package com.aims.logic.sdk.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Component
@Slf4j
public class TransactionalUtils {
    @Autowired
    private PlatformTransactionManager dataSourceTransactionManager;

    /**
     * 开启默认事务
     *
     * @return
     */
    public TransactionStatus newTran() {
        //事务隔离级别属于 mysql
        //传播行为属于 Spring，传播行为是指在 Spring 中，a 方法使用到事务，传到 b 方法中也使用到事务
        DefaultTransactionAttribute defaultTransactionAttribute = new DefaultTransactionAttribute();
//        defaultTransactionAttribute.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        defaultTransactionAttribute.setName(UUID.randomUUID().toString());
        TransactionStatus transaction = dataSourceTransactionManager.getTransaction(defaultTransactionAttribute);
        log.info("开启默认事务:{}", TransactionSynchronizationManager.getCurrentTransactionName());
        return transaction;
    }

    /**
     * 开启新事务
     *
     * @return
     */
    public TransactionStatus newRequiresNewTran() {
        DefaultTransactionAttribute defaultTransactionAttribute = new DefaultTransactionAttribute();
        defaultTransactionAttribute.setPropagationBehavior(DefaultTransactionAttribute.PROPAGATION_REQUIRES_NEW);
        defaultTransactionAttribute.setName(UUID.randomUUID().toString());
        TransactionStatus transaction = dataSourceTransactionManager.getTransaction(defaultTransactionAttribute);
        log.info("开启新事务REQUIRES_NEW:{}", TransactionSynchronizationManager.getCurrentTransactionName());
        return transaction;
    }

    //提交事务
    public void commit(TransactionStatus transaction) {
        if (!transaction.isCompleted()) {
            log.info("提交事务:{}", TransactionSynchronizationManager.getCurrentTransactionName());
            dataSourceTransactionManager.commit(transaction);
        }
    }

    //回滚事务
    public void rollback(TransactionStatus transaction) {
        if (!transaction.isCompleted()) {
            log.info("回滚事务:{}", TransactionSynchronizationManager.getCurrentTransactionName());
            dataSourceTransactionManager.rollback(transaction);
        }
    }
}
