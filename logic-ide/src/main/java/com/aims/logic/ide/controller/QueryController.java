package com.aims.logic.ide.controller;

import com.aims.datamodel.core.dsl.DataModel;
import com.aims.datamodel.core.sqlbuilder.input.QueryInput;
import com.aims.logic.sdk.dto.ApiResult;
import com.aims.logic.sdk.service.LogicService;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ide/data/{dataModelId}")
public class QueryController {
    @Autowired
    private LogicService logicService;

    @PostMapping("/query")
    public ApiResult<Page<Map<String, Object>>> queryByJson(@PathVariable("dataModelId") String dataModelId, @RequestBody String json) {
        QueryInput input = JSONObject.parseObject(json, QueryInput.class);
        if (input.getFrom() == null)
            input.setFrom(new DataModel().setMainTable(dataModelId));
        return new ApiResult<Page<Map<String, Object>>>().setData(logicService.selectPageByInput(input));
    }

//    @PostMapping("/queryBySql")
//    public ApiResult<List<Map<String, Object>>> queryBySql(@RequestBody JSONObject body) {
//        String sql = body.getString("sql");
//        return new ApiResult<List<Map<String, Object>>>().setData(logicService.selectBySql(sql));
//    }
}
