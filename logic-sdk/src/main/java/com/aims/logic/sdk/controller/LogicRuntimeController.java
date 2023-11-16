package com.aims.logic.sdk.controller;

import com.aims.logic.runtime.service.LogicRunnerService;
import com.aims.logic.sdk.BizLogicRunner;
import com.aims.logic.sdk.LogicRunner;
import com.aims.logic.sdk.dto.ApiResult;
import com.aims.logic.util.RuntimeUtil;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author liukun
 */
@RestController
public class LogicRuntimeController {
    private final LogicRunnerService runner;

    public LogicRuntimeController(LogicRunnerService _runner
    ) {
        this.runner = _runner;
    }


    @PostMapping("/api/runtime/logic/v1/run-api/{id}")
    public ApiResult run(@RequestHeader Map<String, String> headers, @RequestBody(required = false) JSONObject body, @PathVariable String id, @RequestParam(value = "debug", required = false, defaultValue = "false") boolean debug) {
        JSONObject headerJson = JSONObject.from(headers);
        JSONObject customEnv = new JSONObject();
        customEnv.put("HEADERS", headerJson);
        var rep = runner.run(id, body, customEnv);
        var res = ApiResult.fromLogicRunResult(rep);
        if (debug) {
            res.setDebug(rep.getLogicLog());
        }
        return res;
    }

    @PostMapping("/api/runtime/logic/v1/run-biz/{id}/{bizId}")
    public ApiResult runBiz(@RequestHeader Map<String, String> headers, @RequestBody(required = false) JSONObject body, @PathVariable String id, @PathVariable String bizId, @RequestParam(value = "debug", required = false, defaultValue = "false") boolean debug) {
        JSONObject headerJson = JSONObject.from(headers);
        JSONObject customEnv = new JSONObject();
        customEnv.put("HEADERS", headerJson);
        var rep = runner.runBiz(id, bizId, body, customEnv);
        var res = ApiResult.fromLogicRunResult(rep);
        if (debug) {
            res.setDebug(rep.getLogicLog());
        }
        return res;
    }

    @PostMapping("/api/runtime/logic/v1/biz/{id}/{startCode}/{bizId}")
    public ApiResult runBizByStartCode(@RequestHeader Map<String, String> headers, @RequestBody(required = false) JSONObject body, @PathVariable String id, @PathVariable String bizId, @PathVariable String startCode, @RequestParam(value = "debug", required = false, defaultValue = "false") boolean debug) {
        JSONObject headerJson = JSONObject.from(headers);
        JSONObject customEnv = new JSONObject();
        customEnv.put("HEADERS", headerJson);
        var rep = runner.runBizByCode(id, bizId, startCode, body, customEnv);
        var res = ApiResult.fromLogicRunResult(rep);
        if (debug) {
            res.setDebug(rep.getLogicLog());
        }
        return res;
    }

    @PutMapping("/api/runtime/logic/v1/update-file/{id}")
    public ApiResult updateFile(@RequestBody(required = false) String body, @PathVariable String id) {
        RuntimeUtil.saveLogicConfigToFile(id, body);
        return new ApiResult();
    }

    /**
     * 查看环境变量配置
     *
     * @return 环境变量json
     */
    @GetMapping("/api/runtime/env")
    public JSONObject env() {
        return RuntimeUtil.getEnvJson();
    }
}
