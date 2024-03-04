package com.aims.logic.runtime.runner.functions.impl;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dto.LogicItemRunResult;
import com.aims.logic.runtime.runner.FunctionContext;
import com.aims.logic.runtime.runner.Functions;
import com.aims.logic.runtime.runner.functions.ILogicItemFunctionRunner;
import com.aims.logic.runtime.service.LogicRunnerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author liukun
 */
@Slf4j
@Service
public class SwitchFunction implements ILogicItemFunctionRunner {

    public SwitchFunction(LogicRunnerService runnerService) {
    }

    @Override
    public LogicItemRunResult invoke(FunctionContext ctx, Object item) {
        try {
            var itemDsl = ((LogicItemTreeNode) item);
            Object conditionObj = Functions.runJsByContext(ctx, "return  " + itemDsl.getCondition());
            String res = conditionObj == null ? null : conditionObj.toString();
            log.info("bizId:{},switch表达式值：{}", ctx.getBizId(), res);
            LogicItemRunResult ret = new LogicItemRunResult();
            AtomicReference<String> nextId = new AtomicReference<>("");
            AtomicReference<String> defNextId = new AtomicReference<>("");
            itemDsl.getBranches().forEach(b -> {
                if (b.getWhen() != null) {
                    if (b.getWhen().equals(res)) {
                        nextId.set(b.getNextId());
                        log.info("bizId:{},命中：{}", ctx.getBizId(), b.getWhen());
                        ret.setMsg("命中：" + b.getWhen());
                    }
                } else {//default节点没有when属性
                    defNextId.set(b.getNextId());
                }
            });
            if (nextId.get().isBlank()) {
                nextId.set(defNextId.get());//when条件未匹配成功，分配默认节点
                ret.setMsg("命中default，表达式值：" + res);
                log.info("bizId:{},命中：default，表达式值：{}", ctx.getBizId(), res);
            }
            return ret.setData(nextId.get());
        } catch (Exception e) {
            ctx.setHasErr(true);
            ctx.setErrMsg(e.getLocalizedMessage());
            return new LogicItemRunResult().setData(e.toString()).setMsg(e.toString());
        }
    }

    @Override
    public String getItemType() {
        return "switch";
    }
}
