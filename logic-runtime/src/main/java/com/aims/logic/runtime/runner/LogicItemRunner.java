package com.aims.logic.runtime.runner;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dto.LogicItemRunResult;
import com.aims.logic.runtime.contract.logger.LogicItemLog;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public class LogicItemRunner {
    final LogicItemTreeNode dsl;

    public LogicItemRunner(LogicItemTreeNode _dsl) {
        dsl = JSON.parseObject(JSON.toJSONString(_dsl), LogicItemTreeNode.class);
    }

    public LogicItemRunResult run(FunctionContext ctx) {
        LogicItemRunResult ret = new LogicItemRunResult();
        LogicItemLog itemLog = new LogicItemLog().
                setName(dsl.getName());
        log.info("[{}]bizId:{},执行节点[{}]", ctx.getLogicId(), ctx.getBizId(), this.dsl.getName());
        log.debug("[{}]bizId:{},上下文 {}", ctx.getLogicId(), ctx.getBizId(), JSONObject.toJSONString(ctx, JSONWriter.Feature.WriteNulls));
        var itemType = this.dsl.getType();
        itemLog.setBeginTime(LocalDateTime.now());
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
            case "assign-global":
                var globalVar = this.dsl.getUrl();
                var globalValue = this.dsl.getBody();
                ret = Functions.get("js").invoke(ctx, String.format("_global.%s = %s", globalVar, globalValue));
                // 获取变量赋值过后的值
                ret.setData(Functions.get("js").invoke(ctx, String.format("return _global.%s", globalVar)).getData());
                break;
            case "assign-local":
                var localVar = this.dsl.getUrl();
                var localValue = this.dsl.getBody();
                ret = Functions.get("js").invoke(ctx, String.format("_var.%s = %s", localVar, localValue));
                // 获取变量赋值过后的值
                ret.setData(Functions.get("js").invoke(ctx, String.format("return _var.%s", localVar)).getData());
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
            itemLog.setConfigInstance(ret.getItemInstance())
                    .setEndTime(LocalDateTime.now())
                    .setConfig(dsl)
                    .setMsg(ret.getMsg())
                    .setReturnData(ret.getData())
                    .setSuccess(ret.isSuccess());
            ret.setItemLog(itemLog);
        }
        return ret;
    }
}
