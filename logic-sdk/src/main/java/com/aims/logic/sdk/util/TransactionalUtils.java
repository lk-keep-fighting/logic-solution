package com.aims.logic.sdk.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

@Component
@Slf4j
public class TransactionalUtils {
    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;

    //开启事务
    public TransactionStatus newTran() {
        //事务隔离级别属于 mysql
        //传播行为属于 Spring，传播行为是指在 Spring 中，a 方法使用到事务，传到 b 方法中也使用到事务
        TransactionStatus transaction = dataSourceTransactionManager.getTransaction(new DefaultTransactionAttribute());
        return transaction;
    }

    //提交事务
    public void commit(TransactionStatus transaction) {
        dataSourceTransactionManager.commit(transaction);
    }

    //回滚事务
    public void rollback(TransactionStatus transaction) {
        dataSourceTransactionManager.rollback(transaction);
    }
}
