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
        log.info("[{}]bizId:{},执行节点:{}", ctx.getLogicId(), ctx.getBizId(), this.dsl.getName());
        log.debug("[{}]bizId:{},上下文 {}", ctx.getLogicId(), ctx.getBizId(), JSONObject.toJSONString(ctx));
        var itemType = this.dsl.getType();
        var originConfig = JSON.copy(this.dsl);
        switch (itemType) {
            case "end":
                ret = Functions.get(itemType).invoke(ctx, this.dsl.getScript() != null ? this.dsl.getScript() : "return _ret");
                break;
            case "wait":
                if (this.dsl.getTimeout() != null) {
                    try {
                        var timeout = Long.parseLong(this.dsl.getTimeout());
                        if (timeout > 0) {
                            Thread.sleep(timeout);
                        }
                    } catch (InterruptedException exception) {
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
        if (ctx.isHasErr()) {
            ret.setSuccess(false).setMsg(ctx.getErrMsg());
        }
        ret.setItemLog(new LogicItemLog()
                .setName(dsl.getName())
                .setConfigInstance(dsl)
                .setConfig(originConfig)
                .setParamsJson(JSONObject.from(ctx.get_par()))
                .setReturnData(ret.getData())
                .setSuccess(ret.isSuccess()));
        return ret;
    }
}
