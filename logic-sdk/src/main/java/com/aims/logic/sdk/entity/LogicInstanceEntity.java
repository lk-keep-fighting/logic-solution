package com.aims.logic.sdk.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Accessors(chain = true)
@TableName("logic_instance")
public class LogicInstanceEntity extends Model<LogicInstanceEntity> {
    @TableId
    private String id;
    @TableField("logicId")
    private String logicId;
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
    private boolean success;
    @TableField("isOver")
    private boolean isOver;
    private String message;
    @TableField("messageId")
    private String messageId;
    @TableField("serverTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime serverTime;
    private String env;
}
