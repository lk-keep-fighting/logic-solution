package com.aims.logic.sdk.service;

import com.aims.logic.runtime.contract.dto.LogicRunResult;
import com.aims.logic.runtime.contract.enums.EnvEnum;
import com.aims.logic.sdk.entity.LogicLogEntity;
import com.aims.logic.sdk.mapper.LogicLogMapper;
import com.aims.logic.sdk.util.RuntimeUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogicLogService {
    private final LogicLogMapper instanceLogMapper;

    @Autowired
    public LogicLogService(LogicLogMapper _instanceLogMapper) {
        this.instanceLogMapper = _instanceLogMapper;
    }

    public void addLog(LogicRunResult res) {
        try {
            EnvEnum env = RuntimeUtil.getEnv().getNODE_ENV();
            com.aims.logic.runtime.contract.log.LogicLog logicLog = res.getLogicLog();
            var nextId = logicLog.getNextItem() == null ? null : logicLog.getNextItem().getId();
            var nextName = logicLog.getNextItem() == null ? null : logicLog.getNextItem().getName();
            LogicLogEntity logEntity = new LogicLogEntity()
                    .setSuccess(res.isSuccess())
                    .setMessage(res.getMsg())
                    .setBizId(logicLog.getBizId())
                    .setVersion(logicLog.getVersion())
                    .setItemLogs(JSON.toJSONString(logicLog.getItemLogs()))
                    .setReturnData(res.getDataString())
                    .setLogicId(logicLog.getLogicId())
                    .setParamsJson(logicLog.getParamsJson() == null ? null : logicLog.getParamsJson().toJSONString())
                    .setVarsJson(logicLog.getVarsJson() == null ? null : logicLog.getVarsJson().toJSONString())
                    .setVarsJsonEnd(logicLog.getVarsJson_end() == null ? null : logicLog.getVarsJson_end().toJSONString())
                    .setNextId(nextId)
                    .setNextName(nextName)
                    .setOver(logicLog.isOver())
                    .setEnv(env == null ? "未知" : env.getValue());
            instanceLogMapper.insert(logEntity);
        } catch (Exception ex) {
            System.err.println("添加日志异常");
            System.err.println(ex);
        }

    }

    public List<LogicLogEntity> queryLogs(String logicId) {
        QueryWrapper<LogicLogEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("logicId", logicId);
        wrapper.orderByDesc("id");
        return instanceLogMapper.selectList(wrapper);
    }

    public LogicLogEntity findLastBizLog(String logicId, String bizId) {
        QueryWrapper<LogicLogEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("logic_id", logicId);
        wrapper.eq("biz_id", bizId);
        wrapper.orderByDesc("server_time").last("LIMIT 1");
        return instanceLogMapper.selectOne(wrapper);
    }

    public int deleteLog(long aid) {
        return instanceLogMapper.deleteById(aid);
    }
}
