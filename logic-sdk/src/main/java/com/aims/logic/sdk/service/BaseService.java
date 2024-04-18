package com.aims.logic.sdk.service;

import com.aims.datamodel.core.sqlbuilder.input.QueryInput;
import com.aims.logic.sdk.dto.FormQueryInput;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface BaseService<T> extends IService<T> {
    Page<T> selectPage(FormQueryInput input);

    //    Page<Map<String, Object>> selectPageByJson(String json);
    Page<Map<String, Object>> selectPageByInput(QueryInput input);

    List<Map<String, Object>> selectBySql(String sql);
}
