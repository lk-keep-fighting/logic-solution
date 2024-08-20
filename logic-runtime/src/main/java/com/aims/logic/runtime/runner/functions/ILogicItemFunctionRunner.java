package com.aims.logic.runtime.runner.functions;

import com.aims.logic.runtime.contract.dto.LogicItemRunResult;
import com.aims.logic.runtime.runner.FunctionContext;

/**
 * @author liukun
 */
public interface ILogicItemFunctionRunner {
    LogicItemRunResult invoke(FunctionContext ctx, Object obj1);

    /**
     * 返回实现的节点类型
     *
     * @return
     */
    String getItemType();

    /**
     * 加载优先级，节点类型相同，数值大的可以覆盖数值小的
     * 可以通过这种方式覆盖默认实现
     *
     * @return
     */
    int getPriority(String env);
}
