package com.aims.logic.ide.controller;

import com.aims.logic.ide.controller.dto.ApiResult;
import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dsl.LogicTreeNode;
import com.aims.logic.runtime.runner.LogicRunner;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class LogicItemController {


    @PostMapping("/api/ide/logic-item/debug")
    public ApiResult runItem(@RequestBody(required = false) JSONObject body) {
        ApiResult res;
        try {
            LogicItemTreeNode itemConfig = body.getObject("config", LogicItemTreeNode.class);
            itemConfig.setId("2");
            JSONObject par = body.getJSONObject("body");
            LogicTreeNode logicConfig = new LogicTreeNode();
            LogicItemTreeNode startItem = new LogicItemTreeNode();
            startItem.setId("1");
            startItem.setType("start");
            startItem.setNextId(itemConfig.getId());
            List<LogicItemTreeNode> items = new ArrayList<>();
            items.add(startItem);
            items.add(itemConfig);
            logicConfig.setItems(items);
            LogicRunner runner = new LogicRunner(JSONObject.from(logicConfig), new JSONObject(), new JSONObject());
            var ret = runner.run(par);
            res = ApiResult.fromLogicRunResult(ret);
        } catch (Exception e) {
            res = ApiResult.fromException(e);
        }
        return res;
    }
}
