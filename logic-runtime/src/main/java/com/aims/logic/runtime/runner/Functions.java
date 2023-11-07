package com.aims.logic.runtime.runner;

import com.aims.logic.runtime.runner.functions.LogicItemFunctionRunnerService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liukun
 */
public class Functions {
    static final Map<String, LogicItemFunctionRunnerService> functions = new HashMap<>();


//    static {
//        functions.put("js", ServiceLoader.load(IJSFunction.class).findFirst().get());
//        functions.put("http", ServiceLoader.load(IHttpFunction.class).findFirst().get());
//    }

    public Functions() {
    }

    public static LogicItemFunctionRunnerService get(String name) {
        return functions.get(name);
    }
}
