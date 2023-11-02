package com.aims.logic.service.controller;

import com.aims.logic.sdk.util.RuntimeUtil;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//@RestController
public class StartupController {

    @GetMapping("/")
    public String hello() {
        return "逻辑编排 java-runtime";
    }
    @GetMapping("/health")
    public JSONObject isOk() {
        return new JSONObject();
    }

    /**
     * 查看环境变量配置
     * @return 环境变量json
     */
    @GetMapping("/env")
    public JSONObject env() {
        return RuntimeUtil.getEnvJson();
    }
}
