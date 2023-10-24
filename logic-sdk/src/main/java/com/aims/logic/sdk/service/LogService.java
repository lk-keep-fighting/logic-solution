package com.aims.logic.sdk.service;

import com.aims.logic.runtime.contract.dto.LogicRunResult;
import com.aims.logic.runtime.contract.enums.EnvEnum;
import com.aims.logic.sdk.entity.LogicRuntimeLog;
import com.aims.logic.sdk.mapper.LogicRuntimeLogMapper;
import com.aims.logic.sdk.util.RuntimeUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogService {
    private final LogicRuntimeLogMapper runtimeLogMapper;

    @Autowired
    public LogService(LogicRuntimeLogMapper _runtimeLogMapper) {
        this.runtimeLogMapper = _runtimeLogMapper;
    }

    public void addLog(LogicRunResult res) {
        try {
            EnvEnum env = RuntimeUtil.GetEnv().getNODE_ENV();
            LogicRuntimeLog runtimeLog = new LogicRuntimeLog()
                    .setSuccess(res.isSuccess())
                    .setMessage(res.getMsg())
                    .setBizId(res.getBizId())
                    .setVersion(res.getVersion())
                    .setDebug(JSON.toJSONString(res.getLogicLog()))
                    .setData(res.getDataString())
                    .setLogicId(res.getLogicId())
                    .setEnv(env == null ? "未知" : env.getValue());
            runtimeLogMapper.insert(runtimeLog);
        } catch (Exception ex) {
            System.err.println("添加日志异常");
            System.err.println(ex);
        }

    }

    public List<LogicRuntimeLog> queryLogs(String logicId) {
        QueryWrapper<LogicRuntimeLog> wrapper = new QueryWrapper<>();
        wrapper.eq("logicId", logicId);
        wrapper.orderByDesc("aid");
        return runtimeLogMapper.selectList(wrapper);
    }

    public int deleteLog(long aid) {
        return runtimeLogMapper.deleteById(aid);
    }
}
