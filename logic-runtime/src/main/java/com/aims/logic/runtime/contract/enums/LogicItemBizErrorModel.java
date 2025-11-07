package com.aims.logic.runtime.contract.enums;

import lombok.Getter;

@Getter
public enum LogicItemBizErrorModel {
    /**
     * 默认业务错误处理,来自全局变量配置，
     * 缺省值为stop
     */
    def("def"),
    /**
     * 停止业务处理
     */
    stop("stop"),
    /**
     * 忽略业务错误
     */
    ignore("ignore");


    final String value;

    LogicItemBizErrorModel(String value) {
        this.value = value;
    }
}
