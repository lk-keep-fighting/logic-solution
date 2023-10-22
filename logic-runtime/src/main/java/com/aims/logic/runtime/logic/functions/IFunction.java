package com.aims.logic.runtime.logic.functions;

import com.aims.logic.runtime.logic.FunctionContext;

public interface IFunction {
    Object invoke(FunctionContext ctx, Object obj1);
}
