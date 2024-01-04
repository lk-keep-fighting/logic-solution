package com.aims.logic.runtime.runner.functions.impl;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dto.LogicItemRunResult;
import com.aims.logic.runtime.runner.FunctionContext;
import com.aims.logic.runtime.runner.Functions;
import com.aims.logic.runtime.runner.functions.ILogicItemFunctionRunner;
import com.aims.logic.runtime.service.LogicRunnerService;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Service;

/**
 * @author liukun
 */
@Service
public class SubLogicFunction implements ILogicItemFunctionRunner {
    LogicRunnerService runnerService;

    public SubLogicFunction(LogicRunnerService runnerService) {
        this.runnerService = runnerService;
    }

    @Override
    public LogicItemRunResult invoke(FunctionContext ctx, Object item) {
        try {
            var itemDsl = ((LogicItemTreeNode) item);
            Object data = Functions.get("js").invoke(ctx, itemDsl.getBody()).getData();
            String subLogicId = itemDsl.getUrl();
            JSONObject jsonData = data == null ? null : JSONObject.from(data);
            var res = runnerService.newInstance(ctx.get_env()).runBizByMap(subLogicId, ctx.getBizId(), jsonData);
            return new LogicItemRunResult().setData(res.getData());
        } catch (Exception e) {
            ctx.setHasErr(true);
            ctx.setErrMsg(e.getLocalizedMessage());
            return new LogicItemRunResult().setData(e.toString()).setMsg(e.toString());
        }
    }

    @Override
    public String getItemType() {
        return "sub-logic";
    }
}
