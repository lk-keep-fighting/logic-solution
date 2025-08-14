package com.aims.logic.ide.controller;

import com.aims.logic.ide.controller.dto.ApiResult;
import com.aims.logic.ide.controller.dto.DiffRemoteLogicsDto;
import com.aims.logic.ide.controller.dto.DiffRemoteLogicsInput;
import com.aims.logic.runtime.util.RuntimeUtil;
import com.aims.logic.sdk.dto.FormQueryInput;
import com.aims.logic.sdk.dto.Page;
import com.aims.logic.sdk.service.LogicPublishService;
import com.aims.logic.sdk.service.LogicService;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class PublishController {
    @Autowired
    private LogicService logicService;
    @Autowired
    private LogicPublishService logicPublishService;

    @PostMapping("/api/ide/publish/logic/to-local/{id}")
    public ApiResult<String> publishConfigToLocalFile(@PathVariable String id, @RequestParam @Nullable boolean isHotUpdate) {
        String path = logicService.pubToLocal(id, isHotUpdate);
        return new ApiResult<String>().setData(path);
    }

    @PostMapping("/api/ide/publish/logic/to-local-from-entity-json")
    public ApiResult<String> publishConfigToLocalFromEntityJson(@RequestBody JSONObject configJson, @RequestParam @Nullable boolean isHotUpdate) {
        try {
            String path = logicService.pubToLocalFromEntityJson(configJson, "", isHotUpdate);
            return new ApiResult<String>().setData(path);
        } catch (Exception e) {
            return new ApiResult<String>().setCode(500).setMsg(e.getMessage());
        }

    }

    @PostMapping("/api/ide/publish/logic/to-ide/{id}/{host_name}")
    public ApiResult<String> publishConfigToIdeHost(@PathVariable String id, @PathVariable String host_name, @RequestParam @Nullable boolean isHotUpdate) {
        try {
            var host = RuntimeUtil.getEnvObject().getPUBLISHED_IDE_HOSTS().stream().filter(h -> h.getName().equals(host_name)).findFirst().orElse(null);
            if (host == null)
                return new ApiResult<String>().setCode(500).setMsg("未发现环境" + host_name);
            String path = logicService.pubToIdeHost(id, host.getUrl(), isHotUpdate);
            return new ApiResult<String>().setData(path);
        } catch (Exception e) {
            return new ApiResult<String>().setCode(500).setMsg(e.getMessage());
        }

    }

    @PostMapping("/api/ide/published/logics")
    public ApiResult<Page> publishedLogics(@RequestBody FormQueryInput input) {
        var list = logicPublishService.selectPage(input);
        return new ApiResult<Page>().setData(list);
    }

    @PostMapping("/api/ide/published/logics/diff")
    public ApiResult<List<DiffRemoteLogicsDto>> diffLocalAndRemoteLogics(@RequestBody DiffRemoteLogicsInput input) {
        List<DiffRemoteLogicsDto> diffList = new ArrayList<>();
        var remoteHost = RuntimeUtil.getEnvObject().getPUBLISHED_IDE_HOSTS().stream().filter(h -> h.getName().equals(input.getHostName())).findFirst().get();
        var remoteList = logicService.selectPageFromRemoteIde(remoteHost.getUrl(), input.getQueryInput() == null ? new FormQueryInput() : input.getQueryInput());
        var localList = logicService.selectPage(input.getQueryInput());
        localList.getRecords().forEach(local -> {
            var optional = remoteList.getRecords().stream().filter(remote -> remote.getId().equals(local.getId())).findFirst();
            var dto = new DiffRemoteLogicsDto();
            dto.setId(local.getId());
            dto.setName(local.getName());
            dto.setLocalVersion(local.getVersion());
            dto.setLocalVersionUpdateTime(local.getUpdateTime());
            optional.ifPresentOrElse(remote -> {
                dto.setRemoteVersion(remote.getVersion());
                dto.setRemoteVersionUpdateTime(remote.getUpdateTime());
                if (local.getUpdateTime().isBefore(remote.getUpdateTime())) {
                    dto.setDiffType("低于远程环境");
                } else if (local.getUpdateTime().isAfter(remote.getUpdateTime())) {
                    dto.setDiffType("更新");
                } else {
                    dto.setDiffType("无变更");
                }
            }, () -> {
                dto.setDiffType("新增");
            });
            diffList.add(dto);
        });

        return new ApiResult<List<DiffRemoteLogicsDto>>().setData(diffList);
    }
}
