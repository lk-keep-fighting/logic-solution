package com.aims.logic.runtime.logic;

import com.aims.logic.contract.dsl.LogicItemTreeNode;
import com.aims.logic.contract.dto.LogicRunResult;
import com.alibaba.fastjson2.JSONObject;

public class LogicItemRunner {
    LogicItemTreeNode dsl;
    LogicRunner logicRunner;

    public LogicItemRunner(LogicItemTreeNode _dsl, LogicRunner _logicRunner) {
        dsl = _dsl;
        logicRunner = _logicRunner;
    }

    public LogicRunResult run(FunctionContext ctx) {
        LogicRunResult result = new LogicRunResult();
        Object ret=null;
        System.out.println("执行逻辑节点 " + this.dsl.getName());
        System.out.println("上下文 " + JSONObject.toJSONString(ctx));
        switch (this.dsl.getType()) {
            case "end":
                ret = Functions.get("js").invoke(ctx, this.dsl.getScript() != null ? this.dsl.getScript() : "return _ret");
                break;
            case "http":
                ret = Functions.get("http").invoke(ctx, this.dsl);
                break;
            case "wait":
                if (this.dsl.getTimeout() != null) {
                    try {
                        var timeout = Long.parseLong(this.dsl.getTimeout());
                        if (timeout > 0)
                            this.wait(timeout);
                    } catch (InterruptedException exception) {
                        System.out.println(exception.toString());
                    }
                }
                break;
            case "js":
                ret = Functions.get("js").invoke(ctx, this.dsl.getScript() != null ? this.dsl.getScript() : "");
                break;
            case "start":
            default:
                System.out.println("def item case");
                break;
        }
        if(ctx.isHasErr()){
            result.setSuccess(false).setMsg(ctx.getErrMsg());
        }
        result.setData(ret);
        return result;
    }

}
