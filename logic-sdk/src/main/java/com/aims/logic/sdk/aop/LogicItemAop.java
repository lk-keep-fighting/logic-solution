package com.aims.logic.sdk.aop;

import com.aims.logic.sdk.util.TransactionalUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

@Aspect
@Component
public class LogicItemAop {
    @Autowired
    TransactionalUtils transactionalUtils;

    @Around(value = "@annotation(com.aims.logic.sdk.annotation.LogicItem)")

    public Object around(ProceedingJoinPoint joinPoint) {
        TransactionStatus begin = null;
        try {
            begin = transactionalUtils.begin();
            Object result = joinPoint.proceed();//表示调用的方法
            transactionalUtils.commit(begin);
            return result;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            transactionalUtils.rollback(begin);
            return "fail";
        }
    }
}
