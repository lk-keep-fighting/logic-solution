package com.aims.logic.sdk.service;

import com.aims.datamodel.core.sqlbuilder.input.QueryInput;
import com.aims.logic.sdk.dto.FormQueryInput;
import com.aims.logic.sdk.dto.Page;

import java.util.List;
import java.util.Map;

public interface BaseService<T, TKey> {
    T selectById(TKey id);

    boolean insert(T entity);

    String insertAndGetId(T entity);

    String insertAndGetId(Map<String, Object> valuesMap);

    boolean insert(Map<String, Object> valuesMap);

    int removeById(TKey id);

    int removeByIds(List<TKey> id);

    int updateById(TKey id, Map<String, Object> valuesMap);

    int updateById(TKey id, T entity);

    Page<T> selectPage(FormQueryInput input);

    QueryInput getQueryInput(FormQueryInput input);

    List<Map<String, Object>> selectBySql(String sql);

    Page<Map<String, Object>> selectPageByInput(QueryInput input);

    Page<T> queryPageByInput(QueryInput input);
}
