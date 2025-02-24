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
                    var res = invokeMethod(ctx, itemDsl);
                    log.info("[{}]bizId:{},异步执行完成,success：{}，msg:{}。", ctx.getLogicId(), ctx.getBizId(), res.isSuccess(), res.getMsg());
                } catch (Exception e) {
                    // 处理 invokeMethod 抛出的异常
                    // 例如：logger.error("Error invoking method asynchronously", e);
                    log.error(" [{}]bizId:{},异步执行异常：{}", ctx.getLogicId(), ctx.getBizId(), e.toString());
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
            String bizId;
            itemDsl.setBody(jsonData == null ? null : jsonData.toJSONString());
            Object bizIdObj = Functions.runJsByContext(ctx, "return " + itemDsl.getBizId());
            bizId = bizIdObj == null ? null : bizIdObj.toString();
            var newRunnerService = runnerService.newInstance(ctx.get_env(), ctx.getLogicId(),ctx.getBizId());

            var itemRunResult = new LogicItemRunResult().setItemInstance(itemDsl);
            if (StringUtils.isNotBlank(ctx.getBizId())) {//父流程为实例模式，子逻辑必须为实例模式，判断是否需要公用bizId
                if (bizId == null || "null".equals(bizId)) {//不共用bizId
                    bizId = ctx.getSubLogicRandomBizId();//从上下文生成一个，并缓存在上下文中，防止出现异常时重试
                    itemDsl.setBizId(bizId);//记录运行时配置
//                    if (ctx.getIsRetry()) {
//                        var res = newRunnerService.retryErrorBiz(subLogicId, bizId);
//                        itemRunResult.setSuccess(res.isSuccess()).setMsg(res.getMsg()).setData(res.getData());
//                    } else {
                    var res = newRunnerService.runBizByMap(subLogicId, bizId, jsonData, ctx.getTraceId());
                    itemRunResult.setSuccess(res.isSuccess()).setMsg(res.getMsg()).setData(res.getData());
//                    }
                } else {
                    var res = newRunnerService.runBizByMap(subLogicId, bizId, jsonData, ctx.getTraceId());
                    itemRunResult.setSuccess(res.isSuccess()).setMsg(res.getMsg()).setData(res.getData());
                }
            } else {
                var res = newRunnerService.runByMap(subLogicId, jsonData, ctx.getTraceId());
                itemRunResult.setSuccess(res.isSuccess()).setMsg(res.getMsg()).setData(res.getData());
            }
            ctx.buildSubLogicRandomBizId();//运行完成后生成下一个随机bizId，不同的逻辑不能公用bizId
            return itemRunResult;

        } catch (Exception e) {
            log.error("[{}]bizId:{},复用逻辑执行异常：{}", ctx.getLogicId(), ctx.getBizId(), e.toString());
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
