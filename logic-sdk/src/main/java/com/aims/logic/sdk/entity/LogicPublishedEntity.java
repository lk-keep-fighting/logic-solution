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
@TableName("logic_published")
public class LogicPublishedEntity extends Model<LogicPublishedEntity> {
    @TableId
    long id;
    @TableField("logicId")
    String logicId;
    String name;
    String version;
    String module;
    @TableField("configJson")
    String configJson;
    @TableField("publishTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    LocalDateTime publishTime;
}
