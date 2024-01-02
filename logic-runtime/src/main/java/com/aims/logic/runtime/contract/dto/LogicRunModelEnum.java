package com.aims.logic.runtime.contract.dto;

public enum LogicRunModelEnum {
    /**
     * 函数模式，无状态，不会继续执行
     */
    Fn,
    /**
     * Java事务模式，每次Java代码调用都开启事务，确保节点状态数据与业务状态数据事务一致
     * 当发生异常时，由业务方通过重试可以继续流程
     *
     */
    BizWithTransaction
}
