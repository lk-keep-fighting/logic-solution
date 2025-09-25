package com.aims.logic.ide.controller;

import com.aims.datamodel.core.dsl.DataModel;
import com.aims.datamodel.core.sqlbuilder.input.QueryInput;
import com.aims.logic.ide.controller.dto.ApiResult;
import com.aims.logic.ide.controller.dto.LogicClassMethodDto;
import com.aims.logic.ide.util.LogicItemUtil;
import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.sdk.service.LogicCbbService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/api/ide/logic/cbb")
public class LogicCbbController {
    @Autowired
    LogicCbbService logicCbbService;

    @Autowired
    LogicItemUtil logicItemUtil;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public LogicCbbController() {
    }

    @PostMapping("/submit/local-to-db")
    public ApiResult submitLocalToDb() {
        var groupData = logicItemUtil.readFromCode(false);
        var methodDtos = groupData.values().stream().findFirst().get();
        // 修复：将DTO列表转换为Object[]数组列表
        List<Object[]> batchArgs = methodDtos.stream()
                .map(dto -> new Object[]{
                        dto.getLogicItem().getCbbId(),
                        dto.getName(),
                        dto.getType(),
                        dto.getVersion(),
                        dto.getGroup(),
                        dto.getOrder(),
                        JSON.toJSONString(dto.getLogicItem())
                })
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(
                "insert into logic_cbb(id, `name`, `type`, `version`,`group`,`order`,configJson) values(?,?,?,?,?,?,?)",
                batchArgs
        );
        return ApiResult.ok("提交成功");
    }

    @PostMapping("/getAllGrouped")
    public ApiResult<Map<String, List<LogicClassMethodDto>>> query() {
        QueryInput input = new QueryInput();
        input.setFrom(new DataModel().setMainTable("logic_cbb"));
        Map<String, List<LogicClassMethodDto>> grouped = new HashMap<>();
        logicCbbService.queryPageByInput(input).getRecords()
                .forEach(item -> {
                    var dto = new LogicClassMethodDto();
                    dto.setLogicItem(JSON.parseObject(item.getConfigJson(), LogicItemTreeNode.class));
                    grouped.computeIfAbsent(item.getGroup(), k -> new ArrayList<>());
                    grouped.get(item.getGroup()).add(dto);
                });

        return new ApiResult<Map<String, List<LogicClassMethodDto>>>().setData(grouped);
    }
}
