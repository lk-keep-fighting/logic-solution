package com.aims.logic.sdk.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

@Component
@Slf4j
public class TransactionalUtils {
    @Autowired
    private PlatformTransactionManager dataSourceTransactionManager;

    //开启事务
    public TransactionStatus newTran() {
        //事务隔离级别属于 mysql
        //传播行为属于 Spring，传播行为是指在 Spring 中，a 方法使用到事务，传到 b 方法中也使用到事务
        DefaultTransactionAttribute defaultTransactionAttribute = new DefaultTransactionAttribute();
//        defaultTransactionAttribute.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        TransactionStatus transaction = dataSourceTransactionManager.getTransaction(defaultTransactionAttribute);
        return transaction;
    }

    //提交事务
    public void commit(TransactionStatus transaction) {
        if (!transaction.isCompleted())
            dataSourceTransactionManager.commit(transaction);
    }

    //回滚事务
    public void rollback(TransactionStatus transaction) {
        if (!transaction.isCompleted())
            dataSourceTransactionManager.rollback(transaction);
    }
}
