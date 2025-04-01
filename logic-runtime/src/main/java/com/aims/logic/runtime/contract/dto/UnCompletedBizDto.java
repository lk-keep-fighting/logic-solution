package com.aims.logic.runtime.contract.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class UnCompletedBizDto {
    private String logicId;
    private String bizId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private LocalDateTime createTime;
    private Boolean isRunning;
    private Boolean isSuccess;
    private Boolean isAsync;
    private String parentLogicId;
    private String parentBizId;
}
