package com.aims.logic.sdk.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@TableName("logic_runtime_logs")
public class LogicRuntimeLog {
    private long aid;
    private String logicId;
    private String version;
    private String bizId;
    private String messageId;
    private boolean success;
    private String message;
    private String debug;
    private String data;
    private Date serverTime;
    private String env;
}
