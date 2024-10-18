package com.aims.logic.sdk.dto;

import lombok.Data;

import java.util.List;

@Data
public class Page<T> {
    protected List<T> records;
    protected long total;
    protected long size;
    protected long current;
    protected List<OrderItem> orders;
    protected boolean optimizeCountSql;
    protected boolean searchCount;
    protected boolean optimizeJoinOfCountSql;
    protected String countId;
    protected Long maxLimit;
    public Page() {}
    public Page(long current, long size) {
        this.current = current;
        this.size = size;
    }
}
