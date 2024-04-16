package com.aims.logic.ide.controller;

import com.aims.logic.sdk.dto.ApiResult;
import com.aims.logic.sdk.service.LogicService;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/logic/api")
public class QueryController {
    @Autowired
    private LogicService logicService;

    @PostMapping("/query/by-json")
    public ApiResult<Page<Map<String, Object>>> queryByJson(@RequestBody String json) {
        return new ApiResult<Page<Map<String, Object>>>().setData(logicService.selectPageByJson(json));
    }

    @PostMapping("/query/by-sql")
    public ApiResult<List<Map<String, Object>>> queryBySql(@RequestBody JSONObject body) {
        String sql = body.getString("sql");
        return new ApiResult<List<Map<String, Object>>>().setData(logicService.selectBySql(sql));
    }
}
