package com.aims.logic.runtime.contract.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class UnCompletedBizDto {
    private String logicId;
    private String bizId;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime createTime;
    private Boolean isRunning;
    private Boolean isSuccess;
    private Boolean isAsync;
    private String parentLogicId;
    private String parentBizId;
    private int retryTimes = 0;
}
