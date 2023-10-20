package com.aims.logic.runtime;

import com.aims.logic.runtime.logic.LogicRunner;
import com.aims.logic.util.JsonUtil;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LogicRuntimeApplicationTests {

    @Test
    void contextLoads() {
        test();
    }

    void test() {
        String path = "/static/logics/1.json";
        JSONObject json = JsonUtil.readJsonFile(path);
        String envIdxPath="/static/envs/index.json";
        JSONObject envIdx=JsonUtil.readJsonFile(envIdxPath);
        String _env=envIdx.get("env").toString();
        String envPath=String.format("/static/envs/env.%s.json",_env);
        JSONObject env=JsonUtil.readJsonFile(envPath);
//        InputStream config = getClass().getResourceAsStream(path);
//        if (config == null) {
//            throw new RuntimeException("读取文件失败");
//        } else {
//            json = JSON.parseObject(config, JSONObject.class);
//        }
        LogicRunner runner = new LogicRunner(json,env);
        var p = new JSONObject();
        p.put("code", 2123276);
        var ret = runner.run(p);
        System.out.println("入参");
        System.out.println(runner.getFnCtx().get_par());
        System.out.printf("返回%n success:%s%n data：%s%n msg：%s%n%n", ret.isSuccess(), ret.getData().toString(), ret.getMsg());
    }
}
