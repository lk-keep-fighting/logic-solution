package com.aims.logic.sdk.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@TableName("logic_instance")
public class LogicInstanceEntity extends Model<LogicInstanceEntity> {
    @TableId
    private String id;
    private String logicId;
    private String version;
    private String bizId;
    private String nextId;
    private String nextName;
    private String paramsJson;
    private String varsJson;
    private String varsJsonEnd;
    private String returnData;
    private boolean success;
    private boolean isOver;
    private String message;
    private String messageId;
    private Date serverTime;
    private String env;
}
