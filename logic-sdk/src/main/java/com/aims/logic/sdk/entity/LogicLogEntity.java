package com.aims.logic.sdk.entity;

import com.aims.logic.sdk.annotation.IdType;
import com.aims.logic.sdk.annotation.TableField;
import com.aims.logic.sdk.annotation.TableId;
import com.aims.logic.sdk.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("logic_log")
public class LogicLogEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField("logicId")
    private String logicId;
    @TableField("clientId")
    private String clientId;
    @TableField("host")
    private String host;
    private String version;
    @TableField("bizId")
    private String bizId;
    @TableField("nextId")
    private String nextId;
    @TableField("nextName")
    private String nextName;
    @TableField("paramsJson")
    private String paramsJson;
    @TableField("varsJson")
    private String varsJson;
    @TableField("varsJsonEnd")
    private String varsJsonEnd;
    @TableField("returnData")
    private String returnData;
    private Boolean success;
    @TableField("isOver")
    private Boolean isOver;
    private String message;
    @TableField("messageId")
    private String messageId;
    @TableField("itemLogs")
    private String itemLogs;
    @TableField("serverTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private LocalDateTime serverTime;
    private String env;
}
