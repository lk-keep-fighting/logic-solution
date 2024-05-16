package com.aims.logic.ide.controller;

import com.aims.logic.runtime.service.LogicRunnerService;
import com.aims.logic.runtime.util.StringConcurrencyUtil;
import com.aims.logic.ide.controller.dto.ApiResult;
import com.aims.logic.runtime.util.RuntimeUtil;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author liukun
 */
@RestController
public class LogicRuntimeController {
    private final LogicRunnerService runner;

    public LogicRuntimeController(LogicRunnerService _runner) {
        this.runner = _runner;
    }


    @PostMapping("/api/runtime/logic/v1/run-api/{id}")
    public ApiResult run(@RequestHeader Map<String, String> headers, @RequestBody(required = false) String body, @PathVariable String id, @RequestParam(value = "debug", required = false, defaultValue = "false") boolean debug) {
        ApiResult res;
        try {
            JSONObject headerJson = JSONObject.from(headers);
            JSONObject customEnv = new JSONObject();
            customEnv.put("HEADERS", headerJson);
            var rep = runner.newInstance(customEnv).runByJson(id, body);
            res = ApiResult.fromLogicRunResult(rep);
            if (debug) {
                res.setDebug(rep.getLogicLog());
            }
        } catch (Exception e) {
            res = ApiResult.fromException(e);
        }
        return res;
    }

    @PostMapping("/api/runtime/logic/v1/run-biz/{id}/{bizId}")
    public ApiResult runBiz(@RequestHeader Map<String, String> headers, @RequestBody(required = false) String body, @PathVariable String id, @PathVariable String bizId, @RequestParam(value = "debug", required = false, defaultValue = "false") boolean debug) {
        ApiResult res;
        try {
            JSONObject headerJson = JSONObject.from(headers);
            JSONObject customEnv = new JSONObject();
            customEnv.put("HEADERS", headerJson);
            var rep = runner.newInstance(customEnv).runBizByJson(id, bizId, body);
            res = ApiResult.fromLogicRunResult(rep);
            if (debug) {
                res.setDebug(rep.getLogicLog());
            }
        } catch (Exception e) {
            res = ApiResult.fromException(e);
        }

        return res;
    }

    @PostMapping("/api/runtime/logic/v1/retry-error-biz/{id}/{bizId}")
    public ApiResult retryErrorBiz(@PathVariable String id, @PathVariable String bizId, @RequestParam(value = "debug", required = false, defaultValue = "false") boolean debug) {
        var rep = runner.retryErrorBiz(id, bizId);
        var res = ApiResult.fromLogicRunResult(rep);
        if (debug) {
            res.setDebug(rep.getLogicLog());
        }
        return res;
    }

    @PostMapping("/api/runtime/logic/v1/biz/{id}/{startCode}/{bizId}")
    public ApiResult runBizByStartCode(@RequestHeader Map<String, String> headers, @RequestBody(required = false) String body, @PathVariable String id, @PathVariable String bizId, @PathVariable String startCode, @RequestParam(value = "debug", required = false, defaultValue = "false") boolean debug) {
        JSONObject headerJson = JSONObject.from(headers);
        JSONObject customEnv = runner.getEnvJson();
        customEnv.put("HEADERS", headerJson);
        var rep = runner.newInstance(customEnv).runBizByVerifyCode(id, bizId, startCode, body);
        var res = ApiResult.fromLogicRunResult(rep);
        if (debug) {
            res.setDebug(rep.getLogicLog());
        }
        return res;
    }

    //    @PostMapping("/api/runtime/logic/v1/step-by-step/{id}/{bizId}")
//    public ApiResult runBizStepByStep(@RequestHeader Map<String, String> headers, @RequestBody(required = false) JSONObject body, @PathVariable String id, @PathVariable String bizId, @RequestParam(value = "debug", required = false, defaultValue = "false") boolean debug) {
//        JSONObject headerJson = JSONObject.from(headers);
//        JSONObject customEnv = new JSONObject();
//        customEnv.put("HEADERS", headerJson);
//        var rep = runner.runBizStepByStep(id, bizId, body, customEnv);
//        var res = ApiResult.fromLogicRunResult(rep);
//        if (debug) {
//            res.setDebug(rep.getLogicLog());
//        }
//        return res;
//    }
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
    public ApiResult env() {
        return new ApiResult().setData(RuntimeUtil.getEnvJson());
    }

    @GetMapping("/api/runtime/state")
    public ApiResult state() {
        return new ApiResult().setData(RuntimeUtil.logicConfigStoreService.getLogicConfigCache().stats());
    }

    @GetMapping("/api/runtime/lockKeys")
    public ApiResult lockKeys() {
        return new ApiResult().setData(StringConcurrencyUtil.getLockKeys());
    }
}
