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
import org.slf4j.MDC;
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

        Object data = Functions.runJsByContext(ctx, itemDsl.getBody());
        JSONObject jsonData = data == null ? null : JSONObject.from(data);
        itemDsl.setBody(jsonData == null ? null : jsonData.toJSONString());

        if (itemDsl.isAsync()) {
            var traceId = MDC.get("traceId");
            // 复制上下文和逻辑节点，避免并发问题
            // 不能放在创建线程中，否则会丢失数据
            var ctxClone = JSONObject.from(ctx).to(FunctionContext.class);
            var itemDslClone = JSONObject.from(itemDsl).to(LogicItemTreeNode.class);

            // 异步调用 invokeMethod
            new Thread(() -> {
                try {
                    if (traceId != null)
                        MDC.put("traceId", traceId);
                    log.info("[{}]bizId:{},开始异步执行……", ctxClone.getLogicId(), ctxClone.getBizId());
                    var res = invokeMethod(ctxClone, itemDslClone, jsonData);
                    log.info("[{}]bizId:{},异步执行完成,success：{}，msg:{}。", ctxClone.getLogicId(), ctxClone.getBizId(),
                            res.isSuccess(), res.getMsg());
                } catch (Exception e) {
                    // 处理 invokeMethod 抛出的异常
                    // 例如：logger.error("Error invoking method asynchronously", e);
                    log.error(" [{}]bizId:{},异步执行异常：{}", ctxClone.getLogicId(), ctxClone.getBizId(), e.toString());
                }
            }).start();
            return itemRunResult.setSuccess(true).setMsg("异步执行中");
        } else {
            return invokeMethod(ctx, itemDsl, jsonData);
        }
    }

    public LogicItemRunResult invokeMethod(FunctionContext ctx, LogicItemTreeNode itemDsl, JSONObject jsonData) {
        try {
//            Object data = Functions.runJsByContext(ctx, itemDsl.getBody());
//            JSONObject jsonData = data == null ? null : JSONObject.from(data);
//            itemDsl.setBody(jsonData == null ? null : jsonData.toJSONString());
            String subLogicId = itemDsl.getUrl();
            String subLogicBizId;
            Object bizIdObj = Functions.runJsByContext(ctx, "return " + itemDsl.getBizId());
            subLogicBizId = bizIdObj == null ? null : bizIdObj.toString();
            itemDsl.setBizId(subLogicBizId);
            var newRunnerService = runnerService.newInstance(ctx.get_env(), ctx.getLogicId(), ctx.getBizId(),
                    itemDsl.getTranPropagation(), itemDsl.isAsync());

            var itemRunResult = new LogicItemRunResult().setItemInstance(itemDsl);
            JSONObject globalEnd;
            if (!itemDsl.isBizOff()) {
                if (StringUtils.isNotEmpty(ctx.getBizId())) { // 父流程为实例模式，子逻辑必须为实例模式
                    if (subLogicBizId == null || "null".equals(subLogicBizId)) { // 判断是否自动生成bizId
                        subLogicBizId = ctx.buildSubLogicRandomBizId(); // 自动生成bizId
                        itemDsl.setBizId(subLogicBizId);
                    }
                    // 父流程为实例模式，调用 runBizByMap 方法
                    var res = newRunnerService.runBizByMap(subLogicId, subLogicBizId, jsonData, ctx.getTraceId(),
                            itemDsl.getObjectId(), ctx.get_global());
                    itemRunResult.setSuccess(res.isSuccess()).setMsg(res.getMsg()).setData(res.getData());
                    globalEnd = res.getLogicLog().getGlobalVars();
                } else { // 父逻辑非实例模式
                    if (subLogicBizId == null || "null".equals(subLogicBizId)) { // 判断是否指定了bizId,null或字符串"null"都为未指定
                        // 未指定 bizId，调用 runByMap 方法
                        var res = newRunnerService.runByMap(subLogicId, jsonData, ctx.getTraceId(),
                                itemDsl.getObjectId(), ctx.get_global());
                        itemRunResult.setSuccess(res.isSuccess()).setMsg(res.getMsg()).setData(res.getData());
                        globalEnd = res.getLogicLog().getGlobalVars();
                    } else {
                        // 指定了 bizId，调用 runBizByMap 方法
                        var res = newRunnerService.runBizByMap(subLogicId, subLogicBizId, jsonData, ctx.getTraceId(),
                                itemDsl.getObjectId(), ctx.get_global());
                        itemRunResult.setSuccess(res.isSuccess()).setMsg(res.getMsg()).setData(res.getData());
                        globalEnd = res.getLogicLog().getGlobalVars();
                    }
                }
            } else {
                var res = newRunnerService.runByMap(subLogicId, jsonData, ctx.getTraceId(),
                        itemDsl.getObjectId(), ctx.get_global());
                itemRunResult.setSuccess(res.isSuccess()).setMsg(res.getMsg()).setData(res.getData());
                globalEnd = res.getLogicLog().getGlobalVars();
            }
            // if (itemRunResult.isSuccess())
            // ctx.buildSubLogicRandomBizId();//运行完成后生成下一个随机bizId保存在临时变量中
            ctx.set_global(globalEnd);
            return itemRunResult;

        } catch (Exception e) {
            log.error("[{}]bizId:{},复用逻辑执行异常：{}", ctx.getLogicId(), ctx.getBizId(), e.toString());
            e.printStackTrace();
            return new LogicItemRunResult().setSuccess(false).setData(e.toString()).setMsg(e.toString())
                    .setItemInstance(itemDsl);
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
