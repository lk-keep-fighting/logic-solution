package com.aims.logic.sdk.service;

import com.aims.logic.runtime.contract.logger.LogicLog;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface LoggerHelperService {

    /**
     * 添加实例和日志聚合方法,添加出错不会抛出异常，会在控制台打印
     * 1、addOrUpdateInstance新增或更新运行实例日志；
     * 2、addLogicLog新增执行日志logic_log日志
     *
     * @param logicLog
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    void addOrUpdateInstanceAndAddLogicLog(LogicLog logicLog);

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    void startBizRunning(LogicLog logicLog);

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    void stopBizRunning(LogicLog logicLog);

    /**
     * 新增或更新运行实例日志
     *
     * @param logicLog
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    void addOrUpdateInstance(LogicLog logicLog);

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    String addInstance(LogicLog logicLog);

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    void updateInstance(LogicLog logicLog);

    /**
     * 新增执行日志logic_log日志
     *
     * @param logicLog
     */
    void addLogicLog(LogicLog logicLog);

    //    List<LogicLogEntity> queryLogs(String logicId);
//
//    List<LogicLogEntity> queryBizLogs(String logicId, String bizId) ;
    void clearLog();
}
