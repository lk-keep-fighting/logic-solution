package com.aims.logic.ide.service.controller;

import com.aims.logic.ide.service.util.FormSvcUtil;
import com.aims.logic.ide.service.util.dto.FormQueryInput;
import com.aims.logic.runtime.contract.dsl.LogicTreeNode;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController("/api/ide/asset")
public class BizAssetController {
    @PostMapping("/logic/add")
    public String addLogic(@RequestBody JSONObject body) {
        var res = FormSvcUtil.add("logic", body);
        return res;
    }

    @PutMapping("/logic/edit/{id}")
    public String editLogic(@PathVariable String id, @RequestBody JSONObject body) {
        var res = FormSvcUtil.edit("logic", id, body);
        return res;
    }

    @DeleteMapping("/logic/delete/{id}")
    public String deleteLogic(@PathVariable String id) {
        var res = FormSvcUtil.delete("logic", id);
        return res;
    }

    @GetMapping("/logic/config/{id}")
    public LogicTreeNode getLogicConfig(@PathVariable String id) {
        var ids = new ArrayList<String>();
        ids.add(id);
        var res = FormSvcUtil.query("logic", new FormQueryInput().setIds(ids));
        if (res != null) {
            if (res.isSuccess() && res.getResult() != null && (long) res.getResult().getItems().size() > 0) {
                var config = (JSONObject) res.getResult().getItems().stream().findFirst().get();
                var json = config.get("configJson");
                return JSON.parseObject(json.toString(), LogicTreeNode.class);
            }
        }
        return null;
    }

    @PutMapping("/logic/config/edit/{id}")
    public String editLogicConfig(@PathVariable String id, @RequestBody JSONObject body) {
        JSONObject editingLogic = new JSONObject();
        editingLogic.put("id", id);
        editingLogic.put("configJson", body);
        var res = FormSvcUtil.edit("logic", id, editingLogic);
        return res;
    }
}
