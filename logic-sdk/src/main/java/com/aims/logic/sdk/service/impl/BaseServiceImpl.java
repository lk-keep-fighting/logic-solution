package com.aims.logic.sdk.service.impl;

import com.aims.logic.sdk.dto.ApiResult;
import com.aims.logic.sdk.dto.FormQueryInput;
import com.aims.logic.sdk.entity.LogicEntity;
import com.aims.logic.sdk.entity.LogicInstanceEntity;
import com.aims.logic.sdk.mapper.LogicInstanceMapper;
import com.aims.logic.sdk.service.BaseService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

public class BaseServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements BaseService<T> {

    public Page<T> selectPage(FormQueryInput input) {
        Page<T> page = new Page<>(input.getPage(), input.getPageSize());
        QueryWrapper queryWrapper = new QueryWrapper();
        if (input.getFilters() != null)
            input.getFilters().forEach(v -> {
                if (!v.getValues().isEmpty()) {
                    if (v.getType().equals("="))
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
}