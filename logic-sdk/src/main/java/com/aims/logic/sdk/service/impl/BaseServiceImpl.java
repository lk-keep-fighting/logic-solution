package com.aims.logic.sdk.service.impl;

import com.aims.logic.sdk.dto.FormQueryInput;
import com.aims.logic.sdk.service.BaseService;
import com.aims.lowcode.tools.jsontosql.dto.Query;
import com.aims.lowcode.tools.jsontosql.service.JsonToQuerySqlService;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
public class BaseServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements BaseService<T> {

    @Autowired
    JsonToQuerySqlService jsonToQuerySqlService;
    @Autowired
    JdbcTemplate jdbcTemplate;

    public Page<T> selectPage(FormQueryInput input) {
        Page<T> page = new Page<>(input.getPage(), input.getPageSize());
        QueryWrapper queryWrapper = new QueryWrapper();
        if (input.getFilters() != null)
            input.getFilters().forEach(v -> {
                if (!v.getValues().isEmpty()) {
                    if ("=".equals(v.getType()))
                        queryWrapper.eq(v.getDataIndex(), v.getValues().get(0));
                    else {
                        var likeValue = v.getValues().get(0);
                        if (!likeValue.isBlank())
                            queryWrapper.like(v.getDataIndex(), likeValue);
                    }
                }
            });
        if (input.getOrderBy() != null && !input.getOrderBy().isEmpty()) {
            input.getOrderBy().forEach(o -> {
                if (o.isDesc()) {
                    queryWrapper.orderByDesc(o.getDataIndex());
                } else
                    queryWrapper.orderByAsc(o.getDataIndex());
            });
        }
        return baseMapper.selectPage(page, queryWrapper);
    }

    public Page<Map<String, Object>> selectPageByJson(String json) {
        Query query = JSONObject.parseObject(json, Query.class);
        var sql = jsonToQuerySqlService.toSqlByDto(query);
        var list = jdbcTemplate.queryForList(sql);
        var fromIdex = sql.indexOf("FROM");
        var countSql = "SELECT COUNT(*) FROM " + sql.substring(fromIdex + 4);
        var count = jdbcTemplate.queryForObject(countSql, Long.class);
        var p = new Page<Map<String, Object>>(query.getPage().getNumber(), query.getPage().getSize(), count);
        p.setRecords(list);
        return p;
    }

    public List<Map<String, Object>> selectBySql(String sql) {
        return jdbcTemplate.queryForList(sql);
    }
}