package com.aims.logic.runtime.runner;

import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dsl.LogicTreeNode;
import com.aims.logic.runtime.contract.dto.LogicItemRunResult;
import com.aims.logic.runtime.contract.dto.LogicRunResult;
import com.aims.logic.runtime.contract.dto.RunnerStatusEnum;
import com.aims.logic.runtime.contract.logger.LogicLog;
import com.aims.logic.runtime.contract.parser.TypeAnnotationParser;
import com.aims.logic.runtime.util.JsonUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Accessors(chain = true)
@Setter
@Getter
public class LogicRunner {
    private String startId;
    private LogicLog logicLog = new LogicLog();
    private LogicItemTreeNode startNode;
    private LogicTreeNode logic;
    private RunnerStatusEnum runnerStatus;
    private FunctionContext fnCtx = new FunctionContext();

    public void setStartNode(LogicItemTreeNode startNode) {
        this.startNode = startNode;
        if (startNode != null)
            this.startId = startNode.getId();
    }

    public LogicRunner(JSONObject _config, JSONObject _env) {
        init(_config, _env, null);
    }

    public LogicRunner(JSONObject _config, JSONObject _env, String bizId) {
        init(_config, _env, bizId);
    }

    public LogicRunner(JSONObject _config, JSONObject _env, Map<String, Object> paramsMap, JSONObject varsJson, String runItemId, String bizId) {
        init(_config, _env, bizId);
        this.setStartNode(getStartItem(runItemId));
        fnCtx.set_par(paramsMap);
        fnCtx.set_var(JsonUtil.jsonMerge(varsJson, fnCtx.get_var()));
        logicLog.setParamsJson(fnCtx.get_par() == null ? null : JSONObject.from(fnCtx.get_par()))
                .setVarsJson(fnCtx.get_var() == null ? null : fnCtx.get_var().clone())
                .setEnvsJson(fnCtx.get_env());
    }


    /**
     * 初始化
     *
     * @param _config 逻辑配置
     * @param _env    环境变量
     */
    private void init(JSONObject _config, JSONObject _env, String bizId) {
        logic = _config.toJavaObject(LogicTreeNode.class);
        JSONObject paramsJson = TypeAnnotationParser.ParamsToJson(logic.getParams());
        JSONObject varsJson = TypeAnnotationParser.ParamsToJson(logic.getVariables());
        JSONObject envJson = TypeAnnotationParser.ParamsToJson(logic.getEnvs());
        envJson = JsonUtil.jsonMerge(_env, envJson);
        autoAnalyzeAndAppendEnv(envJson);
        this.fnCtx.set_par(paramsJson);
        this.fnCtx.set_var(varsJson);
        this.fnCtx.set_env(envJson);
        this.fnCtx.setBizId(bizId);
        logicLog.setBizId(bizId);
        System.out.println("初始化成功");
        System.out.printf("默认参数，_par:%s%n", this.fnCtx.get_par());
        System.out.printf("默认局部变量，_var:%s%n", this.fnCtx.get_var());
        System.out.printf("环境变量，_env:%s%n", this.fnCtx.get_env());
    }

    /**
     * 自动分析环境变量需解析的值，并追加解析后的值到环境变量中，如token解析
     */
    private void autoAnalyzeAndAppendEnv(JSONObject envJson) {
        var headers = envJson.getJSONObject("HEADERS");
        if (headers != null) {
            var jwtToken = headers.getString("authorization");
            if (jwtToken != null) {
                String[] strings = jwtToken.split("\\.");
                if (strings.length > 1) {
                    JSONObject beforeJwtInfo = envJson.getJSONObject("JWT");
                    JSONObject tokenJwtInfo = JSON.parseObject(
                            new String(Base64.getDecoder().decode(strings[1]), StandardCharsets.UTF_8),
                            JSONObject.class
                    );
                    envJson.put("JWT", JsonUtil.jsonMerge(beforeJwtInfo, tokenJwtInfo));
                }
            }
            JSONArray headerFilters = envJson.getJSONArray("HEADER_FILTERS");
            if (headerFilters != null) {
                JSONObject newHeaders = new JSONObject();
                headerFilters.forEach(f -> {
                    var fObj = (JSONObject) f;
                    var filterKey = fObj.getString("key");
                    newHeaders.put(filterKey, headers.get(filterKey));
                });
                envJson.put("HEADERS", newHeaders);
            }
        }

    }

    public LogicItemTreeNode findItemById(String itemId) {
        var itemNode = logic.getItems().stream().filter(i -> Objects.equals(i.getId(), itemId)).findFirst();
        return itemNode.orElse(null);
    }

    public LogicItemTreeNode findItemByCode(String itemCode) {
        var itemNode = logic.getItems().stream().filter(i -> Objects.equals(i.getCode(), itemCode)).findFirst();
        return itemNode.orElse(null);
    }

    /**
     * 获取开始节点，若传入节点编号为null则默认为初始节点
     *
     * @param runItemId
     * @return
     */
    public LogicItemTreeNode getStartItem(String runItemId) {
        LogicItemTreeNode itemNode = null;
        if (runItemId != null) {//指定了执行节点
            itemNode = findItemById(runItemId);
            if (itemNode == null) {
                throw new RuntimeException(String.format("未发现执行节点：%s", runItemId));
            }
        } else {//未指定执行节点，从start开始执行
            var defStartNode = logic.getItems().stream().filter(i -> Objects.equals(i.getType(), "start")).findFirst();
            if (defStartNode.isPresent()) {
                itemNode = defStartNode.get();
            } else {
                throw new RuntimeException("未发现开始节点");
            }
        }
        return itemNode;
    }

    /**
     * 继续执行，可传入上一次执行后缓存的局部变量varsJson，
     * 借用局部变量实现状态恢复继续执行
     *
     * @param runItemId  指定执行节点
     * @param paramsJson 本次传入的参数
     * @param varsJson   局部变量
     * @return 返回结果
     */
//    public LogicRunResult continueRun(String runItemId, JSONObject paramsJson, JSONObject varsJson) {
//        if (varsJson != null) {
//            fnCtx.set_var(varsJson);
//        }
//        var runtime = findItem(runItemId);
//        if (runtime == null)
//            throw new RuntimeException(String.format("未发现执行节点：%s", runItemId));
//        this.startNode = runtime;
//        this.startId = runItemId;
//        fnCtx.set_par(JsonUtil.jsonMerge(paramsJson, fnCtx.get_par()));
//        logicLog.setParamsJson(fnCtx.get_par());
//        logicLog.setVarsJson(fnCtx.get_var().clone());
//        var res = this.runItem(startNode, fnCtx);
//        logicLog.setVarsJson_end(fnCtx.get_var());
//        logicLog.setLogicId(logic.getId());
//        logicLog.setVersion(logic.getVersion());
//        res.setLogicLog(logicLog);
//        return res;
//    }

    /**
     * 从start执行逻辑
     *
     * @param paramsMap 入参
     * @return 返回，通过success判断是否执行成功，data为最后一个节点返回的数据
     */
    public LogicRunResult run(Map<String, Object> paramsMap) {
        return run(null, paramsMap, null);
    }

    /**
     * 执行逻辑，可指定节点编号执行
     *
     * @param runItemId 指定编号执行，null则从start开始执行
     * @param paramsMap 入参对象
     * @param varsJson  局部变量对象，null则从配置中读取，传入则与配置中json合并
     * @return 返回，通过success判断是否执行成功，data为最后一个节点返回的数据
     */
    public LogicRunResult run(String runItemId, Map<String, Object> paramsMap, JSONObject varsJson) {
        this.setStartNode(getStartItem(runItemId));
        fnCtx.set_par(paramsMap);
        fnCtx.set_var(JsonUtil.jsonMerge(varsJson, fnCtx.get_var()));
        logicLog.setParamsJson(fnCtx.get_par() == null ? null : JSONObject.from(fnCtx.get_par()))
                .setVarsJson(fnCtx.get_var() == null ? null : fnCtx.get_var().clone())
                .setEnvsJson(fnCtx.get_env());
        LogicItemRunResult itemRes = runItem(startNode);
        var nextItem = findNextItem(startNode);
        while (updateStatus(itemRes, nextItem) == RunnerStatusEnum.Continue) {
            itemRes = runItem(nextItem);
            nextItem = findNextItem(nextItem);
        }
        var res = LogicRunResult.fromItemResult(itemRes);
        logicLog.setVarsJson_end(fnCtx.get_var())
                .setLogicId(logic.getId())
                .setOver(this.runnerStatus == RunnerStatusEnum.End)
                .setNextItem(fnCtx.getNextItem())
                .setVersion(logic.getVersion())
                .setReturnDataStr(res.getDataString())
                .setMsg(res.getMsg())
                .setSuccess(res.isSuccess());
        res.setLogicLog(logicLog);
        return res;
    }

    public LogicItemRunResult runItem(LogicItemTreeNode item) {
        var itemRes = new LogicItemRunner(item).run(fnCtx);
        fnCtx.set_lastRet(itemRes.getData());
        if (item.getReturnAccept() != null && !item.getReturnAccept().isBlank()) {
            Functions.get("js").invoke(fnCtx, String.format("%s=_lastRet", item.getReturnAccept()));
        }
        logicLog.getItemLogs().add(itemRes.getItemLog());
        return itemRes;
    }

    public RunnerStatusEnum updateStatus(LogicItemRunResult itemRes, LogicItemTreeNode nextItem) {
        fnCtx.setNextItem(nextItem);
        if (!itemRes.isSuccess()) {
            this.setRunnerStatus(RunnerStatusEnum.Error);
        } else {
            if (nextItem != null && !nextItem.getId().isBlank()) {
                if (Objects.equals(nextItem.getType(), "wait-for-continue")) {//发现下一个交互节点，本次执行结束
                    this.setRunnerStatus(RunnerStatusEnum.WaitForContinue);
                } else {
                    this.setRunnerStatus(RunnerStatusEnum.Continue);
                }
            } else
                this.setRunnerStatus(RunnerStatusEnum.End);
        }
        return this.getRunnerStatus();
    }

    public LogicItemTreeNode findNextItem(LogicItemTreeNode curItem) {
        AtomicReference<String> nextId = new AtomicReference<>("");
        AtomicReference<LogicItemTreeNode> nextItem = new AtomicReference<>(null);
        switch (curItem.getType()) {
            case "switch"://switch运行时内部解析了分支条件，并返回了命中分支的下一个节点
                nextId.set(fnCtx.get_lastRet().toString());
                break;
            default:
                nextId.set(curItem.getNextId());
                break;
        }
        logic.getItems().stream().filter(i -> Objects.equals(i.getId(), nextId.get())).findFirst().ifPresent(nextItem::set);
        return nextItem.get();
    }

}
