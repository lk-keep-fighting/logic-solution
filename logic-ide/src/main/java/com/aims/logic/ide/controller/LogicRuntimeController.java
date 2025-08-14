package com.aims.logic.ide.controller;

import com.aims.logic.ide.controller.dto.ApiResult;
import com.aims.logic.ide.util.VersionUtil;
import com.aims.logic.runtime.contract.dsl.LogicTreeNode;
import com.aims.logic.runtime.service.LogicRunnerService;
import com.aims.logic.runtime.util.RuntimeUtil;
import com.aims.logic.sdk.util.lock.BizLock;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author liukun
 */
@RestController
public class LogicRuntimeController {
    private final LogicRunnerService runner;
    BizLock bizLock;

    public LogicRuntimeController(LogicRunnerService _runner, BizLock _bizLock) {
        this.runner = _runner;
        this.bizLock = _bizLock;
    }

    @GetMapping("/api/runtime/logic/v1/config/{id}")
    public ApiResult<LogicTreeNode> getRuntimeConfig(@PathVariable String id) {
        var config = RuntimeUtil.readLogicConfig(id);
        return new ApiResult<LogicTreeNode>().setData(config.to(LogicTreeNode.class));
    }

    @PostMapping("/api/runtime/logic/v1/run-api/{id}")
    public ApiResult run(@RequestHeader Map<String, String> headers, @RequestBody(required = false) JSONObject body, @PathVariable String id, @RequestParam(value = "debug", required = false, defaultValue = "false") boolean debug) {
        ApiResult res;
        try {
            JSONObject headerJson = JSONObject.from(headers);
            JSONObject customEnv = runner.getEnvJson();// new JSONObject();
            customEnv.put("HEADERS", headerJson);
            var rep = runner.newInstance(customEnv).runByMap(id, body);
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
    public ApiResult runBiz(@RequestHeader Map<String, String> headers, @RequestBody(required = false) JSONObject body, @PathVariable String id, @PathVariable String bizId, @RequestParam(value = "debug", required = false, defaultValue = "false") boolean debug) {
        ApiResult res;
        try {
            JSONObject headerJson = JSONObject.from(headers);
            JSONObject customEnv = runner.getEnvJson();// new JSONObject();
            customEnv.put("HEADERS", headerJson);
            var rep = runner.newInstance(customEnv).runBizByMap(id, bizId, body);
            res = ApiResult.fromLogicRunResult(rep);
            if (debug) {
                res.setDebug(rep.getLogicLog());
            }
        } catch (Exception e) {
            res = ApiResult.fromException(e);
        }

        return res;
    }

    @PostMapping("/api/runtime/logic/v1/runGetData/{logicId}")
    public Object runByJsonDirect(@PathVariable String logicId, @RequestBody(required = false) String json) {
        JSONObject pars = null;
        if (json == null) {
            pars = new JSONObject();
        } else if (JSON.isValidArray(json)) {
            pars = JSONObject.of("body", JSON.parseArray(json));
        } else pars = JSONObject.parseObject(json);
        var res = runner.runByMap(logicId, pars);
        return res.getData();
    }


    @PostMapping("/api/runtime/logic/v1/resetBiz/{id}/{bizId}")
    public ApiResult resetBizNext(@RequestHeader Map<String, String> headers, @RequestBody(required = false) JSONObject bodyObj, @PathVariable String id, @PathVariable String bizId, @RequestParam(value = "debug", required = false, defaultValue = "false") boolean debug) {
        ApiResult res = new ApiResult();
        try {
            var nextId = bodyObj.get("startNodeId").toString();
            var nextName = bodyObj.get("startNodeName").toString();
            var varsJsonEnd = bodyObj.get("varsJson").toString();
            runner.resetBizInstanceNextId(id, bizId, nextId, nextName, varsJsonEnd);
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

    @PostMapping("/api/runtime/logic/v1/force-complete-biz/{logicId}/{bizId}")
    public ApiResult forceCompleteBiz(@PathVariable String logicId, @PathVariable String bizId) {
        var rep = runner.forceCompleteBiz(logicId, bizId);
        return ApiResult.ok(rep);
    }

    @PostMapping("/api/runtime/logic/v1/retry-longtime-running-biz")
    public ApiResult retryLongtimeRunningBiz(@RequestParam(value = "timeout", required = false, defaultValue = "30") int timeout) {
        var rep = runner.retryLongtimeRunningBiz(timeout);
        return ApiResult.ok(rep);
    }

    @PostMapping("/api/runtime/logic/v1/biz/{id}/{startCode}/{bizId}")
    public ApiResult runBizByStartCode(@RequestHeader Map<String, String> headers, @RequestBody(required = false) JSONObject body, @PathVariable String id, @PathVariable String bizId, @PathVariable String startCode, @RequestParam(value = "debug", required = false, defaultValue = "false") boolean debug) {
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
        RuntimeUtil.saveLogicConfigToFile(id, body, true);
        return new ApiResult();
    }

    /**
     * 查看环境变量配置
     *
     * @return 环境变量json
     */
    @GetMapping("/api/runtime/env")
    public ApiResult env() {
        return new ApiResult().setData(RuntimeUtil.readEnvFromFile());
    }

    @GetMapping("/api/runtime/version")
    public ApiResult getVersion() {
        return new ApiResult().setData(JSONObject.of("version", VersionUtil.getVersion()));
    }

    /**
     * 修改内存中的环境变量，不会覆盖配置文件，重启后会重新从配置文件加载
     *
     * @param customEnv
     * @return
     */
    @PostMapping("/api/runtime/env/set")
    public ApiResult setEnv(@RequestBody JSONObject customEnv) throws Exception {
        RuntimeUtil.saveEnvToFile(customEnv);
        return new ApiResult().setData(RuntimeUtil.getEnvJson());
    }

    @GetMapping("/api/runtime/config/cache")
    public ApiResult configCache() {
        return new ApiResult().setData(RuntimeUtil.logicConfigStoreService.getLogicConfigCache().asMap());
    }

    @DeleteMapping("/api/runtime/config/cache/{key}")
    public ApiResult deleteConfigCache(@PathVariable String key) {
        RuntimeUtil.logicConfigStoreService.getLogicConfigCache().asMap().remove(key);
        return new ApiResult();
    }

    @DeleteMapping("/api/runtime/config/cache/clear")
    public ApiResult clearConfigCache() {
        RuntimeUtil.logicConfigStoreService.getLogicConfigCache().asMap().clear();
        return new ApiResult();
    }

    @GetMapping("/api/runtime/lockKeys")
    public ApiResult lockKeys() {
        return new ApiResult().setData(this.bizLock.getLockKeys());
    }

    @DeleteMapping("/api/runtime/lockKey/setBizStopping/{key}")
    public ApiResult stoppingBiz(@PathVariable String key) {
        try {
            bizLock.setBizStopping(key);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResult.error(e.toString());
        }
        return new ApiResult().setData(key);
    }
}
