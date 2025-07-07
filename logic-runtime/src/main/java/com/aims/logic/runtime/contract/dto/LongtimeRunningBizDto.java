package com.aims.logic.runtime.contract.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class LongtimeRunningBizDto {
    private String logicId;
    private String bizId;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime createTime;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime startTime;
    private String parentLogicId;
    private String parentBizId;
    private Boolean isAsync;
}
