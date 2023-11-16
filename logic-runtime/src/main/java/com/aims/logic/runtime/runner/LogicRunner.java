package com.aims.logic.runtime.runner;

import com.aims.logic.contract.dsl.LogicItemTreeNode;
import com.aims.logic.contract.dsl.LogicTreeNode;
import com.aims.logic.contract.dto.LogicRunResult;
import com.aims.logic.contract.logger.LogicItemLog;
import com.aims.logic.contract.logger.LogicLog;
import com.aims.logic.contract.parser.TypeAnnotationParser;
import com.aims.logic.util.JsonUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
     * @param paramsJson 入参json对象，传入后与配置中的入参json合并
     * @return 返回，通过success判断是否执行成功，data为最后一个节点返回的数据
     */
    public LogicRunResult run(JSONObject paramsJson) {
        return run(null, paramsJson, null);
    }

    /**
     * 执行逻辑，可指定节点编号执行
     *
     * @param runItemId  指定编号执行，null则从start开始执行
     * @param paramsJson 入参json对象，传入后与配置中的入参json合并
     * @param varsJson   局部变量对象，null则从配置中读取，传入则与配置中json合并
     * @return 返回，通过success判断是否执行成功，data为最后一个节点返回的数据
     */
    public LogicRunResult run(String runItemId, JSONObject paramsJson, JSONObject varsJson) {
        this.setStartNode(getStartItem(runItemId));
        fnCtx.set_par(JsonUtil.jsonMerge(paramsJson, fnCtx.get_par()));
        fnCtx.set_var(JsonUtil.jsonMerge(varsJson, fnCtx.get_var()));
        logicLog.setParamsJson(fnCtx.get_par() == null ? null : fnCtx.get_par().clone())
                .setVarsJson(fnCtx.get_var() == null ? null : fnCtx.get_var().clone())
                .setEnvsJson(fnCtx.get_env());
        var res = this.runItem(startNode, fnCtx);
        logicLog.setVarsJson_end(fnCtx.get_var())
                .setLogicId(logic.getId())
                .setVersion(logic.getVersion())
                .setMsg(res.getMsg())
                .setSuccess(res.isSuccess());
        res.setLogicLog(logicLog);
        return res;
    }

    private LogicRunResult runItem(LogicItemTreeNode item, FunctionContext fnCtx) {
        LogicRunResult itemRes = new LogicItemRunner(item).run(fnCtx);
        var itemLog = new LogicItemLog()
                .setName(item.getName())
                .setConfig(item)
                .setParamsJson(fnCtx.get_par())
                .setReturnData(itemRes.getData())
                .setSuccess(itemRes.isSuccess());
        logicLog.getItemLogs().add(itemLog);
        if (!itemRes.isSuccess()) {
            return itemRes;
        }
        fnCtx.set_lastRet(itemRes.getData());
        if (item.getReturnAccept() != null && !item.getReturnAccept().isBlank()) {
            Functions.get("js").invoke(fnCtx, String.format("%s=_lastRet", item.getReturnAccept()));
        }
        var nextItem = findNextItem(item, fnCtx);
        if (nextItem != null && !nextItem.getId().isBlank()) {
            if (Objects.equals(nextItem.getType(), "wait-for-continue")) {//发现下一个交互节点，本次执行结束
                logicLog.setNextItem(nextItem);
                return itemRes;
            } else {
                itemRes = this.runItem(nextItem, fnCtx);
            }
        } else {
            System.out.println("无下级节点！");
            logicLog.setOver(true);
        }
        return itemRes;
    }

    public LogicItemTreeNode findNextItem(LogicItemTreeNode curItem, FunctionContext fnCtx) {
        AtomicReference<String> nextId = new AtomicReference<>("");
        AtomicReference<String> defNextId = new AtomicReference<>("");
        AtomicReference<LogicItemTreeNode> nextItem = new AtomicReference<>(null);
        ;
        switch (curItem.getType()) {
            case "switch":
                String res = Functions.get("js").invoke(fnCtx, "return  " + curItem.getCondition()).toString();
                curItem.getBranches().forEach(b -> {
                    if (b.getWhen() != null) {
                        if (b.getWhen().equals(res)) {
                            nextId.set(b.getNextId());
                        }
                    } else {//default节点没有when属性
                        defNextId.set(b.getNextId());
                    }
                });
                if (nextId.get().isBlank()) {
                    nextId.set(defNextId.get());//when条件未匹配成功，分配默认节点
                }
                break;
            default:
                nextId.set(curItem.getNextId());
                break;
        }
        logic.getItems().stream().filter(i -> Objects.equals(i.getId(), nextId.get())).findFirst().ifPresent(nextItem::set);
        return nextItem.get();
    }

//    public JSONObject run(JSONObject paramsJson) {
//        ScriptEngineManager manager = new ScriptEngineManager();
//        ScriptEngine engine = manager.getEngineByName("js");
//        //动态声明对象参数，匿名类型
//        var v = new Object() {
//            public String a = "2000";
//        };
//        //转换为json对象传入，匿名类型不能直接传入，否则获取不到值
//        JSONObject j = (JSONObject) JSON.toJSON(v);
//        //声明整型变量，非引用类型，看是否会被js改变
//        var i = 1;
//        //将Java变量放入js上下文
//        engine.put("javaVar", j);
//        engine.put("i", i);
//        try {
//            //js内改变Java变量
//            var res = engine.eval("javaVar.a+=\"ddd\";i+=1;");
//            //js内调用看是否改变成功
//            res = engine.eval("print(javaVar.a,i)");
//            //通过函数的方式返回给Java变量，此处只是函数声明，返回为null
//            //想直接获取，可以直接写res=engine.eval("javaVar")
//            engine.eval("function d(){return {a:javaVar.a,i:i}}");
//            Invocable invocable = (Invocable) engine;
//            //调用js函数，获取返回值
//            var jres = (ScriptObjectMirror) invocable.invokeFunction("d");
//            System.out.println("获取js变更后的整型参数");
//            System.out.println(jres.get("i"));
//            System.out.println("java中的整型变量i");
//            System.out.println(i);//整型值未被js改变
//            var jvar = engine.get("javaVar");
//            System.out.println("获取js变更后的对象参数");
//            System.out.println(jvar);
//            System.out.println("java中的对象变量j");
//            System.out.println(j);//对象变量的属性被改变
//
//        } catch (Exception ex) {
//            System.out.println(ex);
//        }
//        return paramsJson;
//    }

}
