package com.aims.logic.service.controller;

import com.aims.logic.sdk.LogicRuntimeSdk;
import com.aims.logic.service.dto.ApiResult;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.web.bind.annotation.*;

@RestController("/api/runtime/logic/v1")
public class LogicRuntimeController {
//    @GetMapping("/")
//    public String hello() {
//        return "logic runtime service";
//    }

    @PostMapping("/run-api/{id}")
    public ApiResult run(@RequestBody JSONObject body, @PathVariable String id, @RequestParam(value = "debug", required = false, defaultValue = "false") boolean debug) {
        var rep = LogicRuntimeSdk.run(id, body);
        var res = ApiResult.fromLogicRunResult(rep);
        if (debug) {
            res.setDebug(rep.getLogicLog());
        }
        return res;
    }


//    @PostMapping("/api/runtime/logic/v1/debug-api/{id}")
//    public ApiResult debug(@RequestBody JSONObject body, @PathVariable String id) {
//        var rep = LogicRuntimeSdk.run(id, body);
//        return ApiResult.fromLogicRunResult(rep).setDebugLog(rep.getLogicLog());
//    }
}
