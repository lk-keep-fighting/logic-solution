package com.aims.logic.ide.service.controller;

import com.aims.logic.contract.dsl.LogicTreeNode;
import com.aims.logic.ide.service.util.FormSvcUtil;
import com.aims.logic.ide.service.util.dto.FormQueryInput;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
public class BizAssetController {
    @GetMapping("/api/ide/asset/logic/{id}")
    public LogicTreeNode GetLogicConfig(@PathVariable String id) {
        var ids = new ArrayList<String>();
        ids.add(id);
        var res = FormSvcUtil.query("logic", new FormQueryInput().setIds(ids));
        if (res != null) {
            if (res.isSuccess() && res.getResult() != null && res.getResult().getItems().stream().count() > 0) {
                var config = (JSONObject) res.getResult().getItems().stream().findFirst().get();
                var json = config.get("configJson");
                return JSON.parseObject(json.toString(), LogicTreeNode.class);
            }
        }
        return null;
    }
}
