package com.aims.logic.sdk.functions;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dto.LogicItemRunResult;
import com.aims.logic.runtime.runner.FunctionContext;
import com.aims.logic.runtime.runner.Functions;
import com.aims.logic.runtime.runner.functions.ILogicItemFunctionRunner;
import com.aims.logic.runtime.service.LogicRunnerService;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author liukun
 */
@Component
@Slf4j
public class SubLogicFunction implements ILogicItemFunctionRunner {
    LogicRunnerService runnerService;

    public SubLogicFunction(LogicRunnerService runnerService) {
        this.runnerService = runnerService;
    }

    @Override
    public LogicItemRunResult invoke(FunctionContext ctx, Object item) {
        var itemDsl = ((LogicItemTreeNode) item);
        var itemRunResult = new LogicItemRunResult().setItemInstance(itemDsl);
        if (itemDsl.isAsync()) {
            // 异步调用 invokeMethod
            new Thread(() -> {
                try {
                    log.info("[{}]bizId:{},开始异步执行……", ctx.getLogicId(), ctx.getBizId());
                    invokeMethod(ctx, itemDsl);
                    log.info("[{}]bizId:{},异步执行完成。", ctx.getLogicId(), ctx.getBizId());
                } catch (Exception e) {
                    // 处理 invokeMethod 抛出的异常
                    // 例如：logger.error("Error invoking method asynchronously", e);
                    log.error("[{}]bizId:{},异步执行异常：{}", ctx.getLogicId(), ctx.getBizId(), e.toString());
                }
            }).start();
            return itemRunResult.setSuccess(true).setMsg("异步执行中");
        } else {
            return invokeMethod(ctx, itemDsl);
        }
    }

    public LogicItemRunResult invokeMethod(FunctionContext ctx, LogicItemTreeNode itemDsl) {
        try {
            Object data = Functions.runJsByContext(ctx, itemDsl.getBody());
            String subLogicId = itemDsl.getUrl();
            JSONObject jsonData = data == null ? null : JSONObject.from(data);
            String bizId = ctx.getBizId();
            itemDsl.setBody(jsonData == null ? null : jsonData.toJSONString());
            if (StringUtils.isBlank(itemDsl.getBizId())) {
                bizId = ctx.getBizId();
            } else {
                Object bizIdObj = Functions.runJsByContext(ctx, "return " + itemDsl.getBizId());
                bizId = bizIdObj == null ? bizId : bizIdObj.toString();
            }
            var newRunner = runnerService.newInstance(ctx.get_env());
            var itemRunResult = new LogicItemRunResult().setItemInstance(itemDsl);
            if (bizId == null || "null".equals(bizId)) {
                var res = newRunner.runByMap(subLogicId, jsonData);
                return itemRunResult.setSuccess(res.isSuccess()).setMsg(res.getMsg()).setData(res.getData());
            } else {
                var res = newRunner.runBizByMap(subLogicId, bizId, jsonData);
                return itemRunResult.setSuccess(res.isSuccess()).setMsg(res.getMsg()).setData(res.getData());
            }

        } catch (Exception e) {
            return new LogicItemRunResult().setSuccess(false).setData(e.toString()).setMsg(e.toString()).setItemInstance(itemDsl);
        }
    }

    @Override
    public String getItemType() {
        return "sub-logic";
    }

    @Override
    public int getPriority(String env) {
        return 1;
    }
}
