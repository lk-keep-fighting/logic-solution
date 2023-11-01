package com.aims.logic.sdk.service;

import com.aims.logic.sdk.dto.FormQueryInput;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface BaseService<T> extends IService<T> {
    Page<T> selectPage(FormQueryInput input);
}
