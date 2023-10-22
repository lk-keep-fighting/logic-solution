package com.aims.logic.runtime.logic;

import com.aims.logic.runtime.logic.functions.HttpFunction;
import com.aims.logic.runtime.logic.functions.IFunction;
import com.aims.logic.runtime.logic.functions.JsFunction;

import java.util.HashMap;
import java.util.Map;

public class Functions {
    static Map<String, IFunction> functions = new HashMap<>();


    static {
        functions.put("js", new JsFunction());
        functions.put("http", new HttpFunction());
    }

    public Functions() {
    }

    public static IFunction get(String name) {
        return functions.get(name);
    }
}
