package com.aims.logic.runtime.runner.functions.impl;

import com.aims.logic.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.runner.FunctionContext;
import com.aims.logic.runtime.runner.Functions;
import com.aims.logic.runtime.runner.LogicRunner;
import com.aims.logic.runtime.runner.functions.SubLogicFunctionService;
import com.aims.logic.runtime.service.LogicRunnerService;
import com.aims.logic.util.FileUtil;
import com.aims.logic.util.RuntimeUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Service;

/**
 * @author liukun
 */
@Service
public class SubLogicFunction implements SubLogicFunctionService {
    LogicRunnerService runnerService;

    public SubLogicFunction(LogicRunnerService runnerService) {
        this.runnerService = runnerService;
    }

    @Override
    public Object invoke(FunctionContext ctx, Object item) {
        try {
            var itemDsl = ((LogicItemTreeNode) item);
            Object data = Functions.get("js").invoke(ctx, itemDsl.getBody());
            String subLogicId = itemDsl.getUrl();
            JSONObject jsonData = data == null ? null : JSONObject.from(data);
//            var config = RuntimeUtil.readLogicConfig(subLogicId);
//            var res = new LogicRunner(config, ctx.get_env()).run(jsonData);
            var res = runnerService.runBiz(subLogicId, ctx.getBizId(), jsonData, ctx.get_env());
            return res.getData();
        } catch (Exception e) {
            ctx.setHasErr(true);
            ctx.setErrMsg(e.getLocalizedMessage());
            return e.toString();
        }
    }

    @Override
    public String getItemType() {
        return "sub-logic";
    }
}
