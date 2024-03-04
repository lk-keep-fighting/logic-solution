package com.aims.logic.runtime.runner;

import com.aims.logic.runtime.runner.functions.ILogicItemFunctionRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liukun
 */
public class Functions {
    static final Map<String, ILogicItemFunctionRunner> functions = new HashMap<>();


//    static {
//        functions.put("js", ServiceLoader.load(IJSFunction.class).findFirst().get());
//        functions.put("http", ServiceLoader.load(IHttpFunction.class).findFirst().get());
//    }

    public Functions() {
    }

    public static Object runJsByContext(FunctionContext ctx, String script) {
        return Functions.get("js").invoke(ctx, script).getData();
    }

    public static ILogicItemFunctionRunner get(String name) {
        return functions.get(name);
    }
}
