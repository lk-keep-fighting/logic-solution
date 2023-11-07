package com.aims.logic.runtime.runner.functions;

import com.aims.logic.runtime.runner.FunctionContext;

/**
 * @author liukun
 */
public interface LogicItemFunctionRunnerService {
    Object invoke(FunctionContext ctx, Object obj1);

    /**
     * 返回实现的节点类型
     * @return
     */
    String getItemType();
}
