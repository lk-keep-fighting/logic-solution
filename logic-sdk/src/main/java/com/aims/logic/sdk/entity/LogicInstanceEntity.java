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
@TableName("logic_instance")
public class LogicInstanceEntity extends BaseEntity {

    public LogicInstanceEntity() {
        createTime = LocalDateTime.now();
    }

    @TableId(type = IdType.UUID)
    private String id;
    @TableField("logicId")
    private String logicId;
    private String version;
    @TableField("bizId")
    private String bizId;
    // 父逻辑id
    @TableField("parentLogicId")
    private String parentLogicId;
    // 父业务标识
    @TableField("parentBizId")
    private String parentBizId;
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
    @TableField("envsJson")
    private String envsJson;
    @TableField("returnData")
    private String returnData;
    private Boolean success;
    // 是否运行中
    @TableField("isRunning")
    private Boolean isRunning;
    // 实例是否结束
    @TableField("isOver")
    private Boolean isOver;
    private String message;
    @TableField("messageId")
    private String messageId;
    /**
     * 创建时间
     */
    @TableField("createTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    @TableField("serverTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime serverTime;
    @TableField("startTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private LocalDateTime startTime;
    @TableField("stopTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private LocalDateTime stopTime;
    // 耗时，单位毫秒
    @TableField("duration")
    private Long duration;
    @TableField("retryTimes")
    private int retryTimes = 0;
    private String env;
    @TableField("isAsync")
    private Boolean isAsync;
}
