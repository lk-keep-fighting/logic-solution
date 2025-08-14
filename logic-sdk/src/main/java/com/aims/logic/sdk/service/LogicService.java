package com.aims.logic.sdk.service;

import com.aims.logic.sdk.dto.FormQueryInput;
import com.aims.logic.sdk.dto.Page;
import com.aims.logic.sdk.entity.LogicEntity;
import com.alibaba.fastjson2.JSONObject;

import java.util.List;
import java.util.Map;

public interface LogicService extends BaseService<LogicEntity, String> {
    /**
     * 编辑并备份
     *
     * @param id
     * @param input
     * @return
     */
    int editAndBak(String id, LogicEntity input);

    /**
     * 发布到本地
     *
     * @param id 路径编号
     * @return 保存路径
     */

    String pubToLocal(String id, boolean isHotUpdate);

    /**
     * 通过Logic实体json发布到本地，用于远程发布
     *
     * @param jsonStr Logic实体json
     * @param source  发布源
     * @return
     */
    String pubToLocalFromEntityJson(JSONObject jsonStr, String source, boolean isHotUpdate);

    String pubToIdeHost(String id, String url, boolean isHotUpdate);

    Page<LogicEntity> selectPageFromRemoteIde(String ideHost, FormQueryInput input);

    List<Map<String, Object>> getModuleList();
}
