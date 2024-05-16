package com.aims.logic.runtime.runner;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dto.LogicItemRunResult;
import com.aims.logic.runtime.contract.logger.LogicItemLog;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogicItemRunner {
    final LogicItemTreeNode dsl;

    public LogicItemRunner(LogicItemTreeNode _dsl) {
        dsl = JSON.parseObject(JSON.toJSONString(_dsl), LogicItemTreeNode.class);
    }

    public LogicItemRunResult run(FunctionContext ctx) {
        LogicItemRunResult ret = new LogicItemRunResult();
        log.info("[{}]bizId:{},执行节点[{}]", ctx.getLogicId(), ctx.getBizId(), this.dsl.getName());
        log.debug("[{}]bizId:{},上下文 {}", ctx.getLogicId(), ctx.getBizId(), JSONObject.toJSONString(ctx));
        var itemType = this.dsl.getType();
//        var originConfig = JSON.copy(this.dsl);
        switch (itemType) {
            case "end":
                ret = Functions.get(itemType).invoke(ctx, this.dsl.getScript() != null ? this.dsl.getScript() : "return _ret");
                break;
            case "wait":
                Double timeout = 0.0;
                if (this.dsl.getTimeout() != null) {
                    try {
                        timeout = Double.parseDouble(Functions.runJsByContext(ctx, "return " + this.dsl.getTimeout()).
                                toString());
                    } catch (Exception ex) {
                        log.error("[{}]bizId:{},延时未执行，wait节点转换延时异常:{}", ctx.getLogicId(), ctx.getBizId(), ex.toString());
                    }
                    try {
                        if (timeout > 0) {
                            log.info("[{}]bizId:{},等待[{}]毫秒", ctx.getLogicId(), ctx.getBizId(), timeout.longValue());
                            Thread.sleep(timeout.longValue());
                        }
                    } catch (InterruptedException exception) {
                        ret.setSuccess(false).setMsg(exception.getMessage());
                        log.error("[{}]bizId:{},wait节点异常:{}", ctx.getLogicId(), ctx.getBizId(), exception.toString());
                    }
                }
                break;
            case "js":
                ret = Functions.get(itemType).invoke(ctx, this.dsl.getScript() != null ? this.dsl.getScript() : "");
                break;
            case "start":
            default:
                var func = Functions.get(itemType);
                if (func != null)
                    ret = func.invoke(ctx, this.dsl);
                else
                    log.debug("[{}]bizId:{},未实现的类型：{}", ctx.getLogicId(), ctx.getBizId(), dsl.getType());
                break;
        }
        log.info("[{}]bizId:{},节点[{}]-返回值：{}", ctx.getLogicId(), ctx.getBizId(), this.dsl.getName(), ret.getData());
        // 关闭日志时不追加，防止循环逻辑或大数据逻辑暴内存
        if (ctx.isLogOff()) {
            log.info("[{}]关闭了日志", ctx.getLogicId());
        } else {
            ret.setItemLog(new
                    LogicItemLog().
                    setName(dsl.getName()).
                    setConfigInstance(ret.getItemInstance()).
                    setConfig(dsl)
//                .setParamsJson(JSONObject.from(dsl.getBody()))
                    .setReturnData(ret.getData())
                    .setSuccess(ret.isSuccess()));
        }
        return ret;
    }
}
