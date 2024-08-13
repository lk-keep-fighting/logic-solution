package com.aims.logic.sdk.aop;

//@Aspect
//@Component
//public class LogicItemAop {
//    @Autowired
//    TransactionalUtils transactionalUtils;
//
//    @Around(value = "@annotation(com.aims.logic.sdk.annotation.LogicItem)")
//
//    public Object around(ProceedingJoinPoint joinPoint) {
//        TransactionStatus begin = null;
//        try {
//            begin = transactionalUtils.newTran();
//            Object result = joinPoint.proceed();//表示调用的方法
//            transactionalUtils.commit(begin);
//            return result;
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//            transactionalUtils.rollback(begin);
//            return "fail";
//        }
//    }
//}
