package com.aims.logic.runtime.logic.functions;

import com.aims.logic.runtime.logic.FunctionContext;

public interface IFunction<T1> {
    Object invoke(FunctionContext ctx, T1 obj1);
}
