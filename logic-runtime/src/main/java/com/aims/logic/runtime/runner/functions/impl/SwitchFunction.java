package com.aims.logic.runtime.runner.functions.impl;

import com.aims.logic.contract.dsl.LogicItemTreeNode;
import com.aims.logic.contract.dto.LogicItemRunResult;
import com.aims.logic.runtime.runner.FunctionContext;
import com.aims.logic.runtime.runner.Functions;
import com.aims.logic.runtime.runner.functions.SwitchFunctionService;
import com.aims.logic.runtime.service.LogicRunnerService;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author liukun
 */
@Service
public class SwitchFunction implements SwitchFunctionService {

    public SwitchFunction(LogicRunnerService runnerService) {
    }

    @Override
    public LogicItemRunResult invoke(FunctionContext ctx, Object item) {
        try {
            var itemDsl = ((LogicItemTreeNode) item);
            Object data = Functions.get("js").invoke(ctx, itemDsl.getBody()).getData();
            String res = Functions.get("js").invoke(ctx, "return  " + itemDsl.getCondition()).getDataString();
            AtomicReference<String> nextId = new AtomicReference<>("");
            AtomicReference<String> defNextId = new AtomicReference<>("");
            itemDsl.getBranches().forEach(b -> {
                if (b.getWhen() != null) {
                    if (b.getWhen().equals(res)) {
                        nextId.set(b.getNextId());
                    }
                } else {//default节点没有when属性
                    defNextId.set(b.getNextId());
                }
            });
            if (nextId.get().isBlank()) {
                nextId.set(defNextId.get());//when条件未匹配成功，分配默认节点
            }
            return new LogicItemRunResult().setData(nextId.get());
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
