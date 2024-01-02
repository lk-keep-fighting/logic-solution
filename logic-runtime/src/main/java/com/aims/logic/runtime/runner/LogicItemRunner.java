package com.aims.logic.runtime.runner;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dto.LogicItemRunResult;
import com.aims.logic.runtime.contract.logger.LogicItemLog;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

public class LogicItemRunner {
    final LogicItemTreeNode dsl;

    public LogicItemRunner(LogicItemTreeNode _dsl) {
        dsl = _dsl;
    }

    public LogicItemRunResult run(FunctionContext ctx) {
        LogicItemRunResult ret = new LogicItemRunResult();
//        Object ret = null;
        System.out.println("执行节点 " + this.dsl.getName());
        System.out.println("上下文 " + JSONObject.toJSONString(ctx));
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
                        System.out.println(exception.toString());
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
                    System.out.println("未实现的类型：" + dsl.getType());
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
