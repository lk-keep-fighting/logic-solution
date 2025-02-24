package com.aims.logic.sdk.service;

import com.aims.logic.runtime.contract.logger.LogicLog;

public interface LoggerHelperService {

    /**
     * 添加实例和日志聚合方法,添加出错不会抛出异常，会在控制台打印
     * 1、addOrUpdateInstance新增或更新运行实例日志；
     * 2、addLogicLog新增执行日志logic_log日志
     *
     * @param logicLog
     */
    void addOrUpdateInstanceAndAddLogicLog(LogicLog logicLog);


    void stopBizRunning(LogicLog logicLog);

    void updateBizResult(String instanceId, boolean success, String msg);

    /**
     * 新增或更新运行实例日志
     *
     * @param logicLog
     */
    void addOrUpdateInstance(LogicLog logicLog);

    String addInstance(LogicLog logicLog);

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
