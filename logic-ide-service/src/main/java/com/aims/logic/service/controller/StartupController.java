package com.aims.logic.service.controller;

import com.aims.logic.runtime.util.RuntimeUtil;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StartupController {

    @GetMapping("/hello")
    public String isOk() {
        return "hi,i'm xiao qiang.";
    }

    /**
     * 查看环境变量配置
     *
     * @return 环境变量json
     */
    @GetMapping("/env")
    public JSONObject env() {
        return RuntimeUtil.getEnvJson();
    }
}
