package com.aims.logic.service.controller;

import com.aims.logic.sdk.LogicRunner;
import com.aims.logic.sdk.util.FileUtil;
import com.aims.logic.sdk.util.RuntimeUtil;
import com.aims.logic.service.dto.ApiResult;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class LogicRuntimeController {
    private final LogicRunner runner;

    public LogicRuntimeController(LogicRunner _runner) {
        this.runner = _runner;
    }


    @PostMapping("/api/runtime/logic/v1/run-api/{id}")
    public ApiResult run(@RequestHeader Map<String, String> headers, @RequestBody(required = false) JSONObject body, @PathVariable String id, @RequestParam(value = "debug", required = false, defaultValue = "false") boolean debug) {
        JSONObject customEnv = new JSONObject();
        customEnv.put("HEADERS", headers);
        var rep = runner.run(id, body, customEnv);
        var res = ApiResult.fromLogicRunResult(rep);
        if (debug) {
            res.setDebug(rep.getLogicLog());
        }
        return res;
    }

    @PostMapping("/api/runtime/logic/v1/runBiz/{id}/{bizId}")
    public ApiResult runBiz(@RequestHeader Map<String, String> headers, @RequestBody(required = false) JSONObject body, @PathVariable String id, @PathVariable String bizId, @RequestParam(value = "debug", required = false, defaultValue = "false") boolean debug) {
        JSONObject customEnv = new JSONObject();
        customEnv.put("HEADERS", headers);
        var rep = runner.runBiz(id, bizId, body, customEnv);
        var res = ApiResult.fromLogicRunResult(rep);
        if (debug) {
            res.setDebug(rep.getLogicLog());
        }
        return res;
    }

    @PutMapping("/api/runtime/logic/v1/updateFile/{id}")
    public ApiResult updateFile(@RequestBody(required = false) String body, @PathVariable String id) {
        RuntimeUtil.saveLogicConfigToFile(id, body);
        return new ApiResult();
    }


//    @PostMapping("/api/runtime/logic/v1/debug-api/{id}")
//    public ApiResult debug(@RequestBody JSONObject body, @PathVariable String id) {
//        var rep = LogicRuntimeSdk.run(id, body);
//        return ApiResult.fromLogicRunResult(rep).setDebugLog(rep.getLogicLog());
//    }
}
