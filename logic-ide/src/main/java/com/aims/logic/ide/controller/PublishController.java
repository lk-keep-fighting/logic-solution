package com.aims.logic.ide.controller;

import com.aims.logic.runtime.util.RuntimeUtil;
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

import javax.servlet.http.HttpServletRequest;
import java.net.http.HttpRequest;

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

    @PostMapping("/api/ide/publish/logic/to-local-from-entity-json")
    public ApiResult<String> publishConfigToLocalFromEntityJson(@RequestBody String configJson, HttpServletRequest request) {
        try {
            String path = logicService.pubToLocalFromEntityJson(configJson, request.getRemoteAddr());
            return new ApiResult<String>().setData(path);
        } catch (Exception e) {
            return new ApiResult<String>().setCode(500).setMsg(e.getMessage());
        }

    }

    @PostMapping("/api/ide/publish/logic/to-ide/{id}/{host_name}")
    public ApiResult<String> publishConfigToIdeHost(@PathVariable String id, @PathVariable String host_name) {
        try {
            var host = RuntimeUtil.getEnvObject().getPUBLISHED_IDE_HOSTS().stream().filter(h -> h.getName().equals(host_name)).findFirst().orElse(null);
            if (host == null)
                return new ApiResult<String>().setCode(500).setMsg("未发现环境" + host_name);
            String path = logicService.pubToIdeHost(id, host.getUrl());
            return new ApiResult<String>().setData(path);
        } catch (Exception e) {
            return new ApiResult<String>().setCode(500).setMsg(e.getMessage());
        }

    }

    @PostMapping("/api/ide/published/logics")
    public ApiResult<Page<LogicPublishedEntity>> publishedLogics(@RequestBody FormQueryInput input) {
        var list = logicPublishService.selectPage(input);
        return new ApiResult<Page<LogicPublishedEntity>>().setData(list);
    }
}
