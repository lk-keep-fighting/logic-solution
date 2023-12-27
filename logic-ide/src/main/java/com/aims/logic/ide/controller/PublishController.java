package com.aims.logic.ide.controller;

import com.aims.logic.sdk.dto.ApiResult;
import com.aims.logic.sdk.dto.FormQueryInput;
import com.aims.logic.sdk.entity.LogicPublishedEntity;
import com.aims.logic.sdk.service.LogicPublishService;
import com.aims.logic.sdk.service.LogicService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublishController {
    @Autowired
    private LogicService logicService;
    @Autowired
    private LogicPublishService logicPublishService;

    @PostMapping("/api/ide/publish/logic/to-local/{id}")
    public ApiResult<String> publishConfigToLocalFile(@PathVariable String id) {
        String path = logicService.pubToLocal(id);
        return new ApiResult<String>().setData(path);
    }

    @PostMapping("/api/ide/published/logics")
    public ApiResult<Page<LogicPublishedEntity>> publishedLogics(@RequestBody FormQueryInput input) {
        var list = logicPublishService.selectPage(input);
        return new ApiResult<Page<LogicPublishedEntity>>().setData(list);
    }
}
