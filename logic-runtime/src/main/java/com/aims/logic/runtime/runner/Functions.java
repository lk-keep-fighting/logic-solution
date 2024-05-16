package com.aims.logic.runtime.runner;

import com.aims.logic.runtime.runner.functions.ILogicItemFunctionRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liukun
 */
@Slf4j
public class Functions {
    static final Map<String, ILogicItemFunctionRunner> functions = new HashMap<>();


//    static {
//        functions.put("js", ServiceLoader.load(IJSFunction.class).findFirst().get());
//        functions.put("http", ServiceLoader.load(IHttpFunction.class).findFirst().get());
//    }

    public Functions() {
    }

    public static Object runJsByContext(FunctionContext ctx, String script) {
        var res = Functions.get("js").invoke(ctx, script);
        if (res.isSuccess())
            return res.getData();
        else {
            var msg = String.format("执行js脚本报错：%s,异常的脚本：%s", res.getMsg(), script);
            log.error(msg);
            throw new RuntimeException(msg);
        }
    }

    public static String runJsExpressByContext(FunctionContext ctx, String script) {
        if (!StringUtils.hasText(script)) return null;
        return Functions.get("js").invoke(ctx, "return " + script).getData().toString();
    }


    public static ILogicItemFunctionRunner get(String name) {
        return functions.get(name);
    }
}
