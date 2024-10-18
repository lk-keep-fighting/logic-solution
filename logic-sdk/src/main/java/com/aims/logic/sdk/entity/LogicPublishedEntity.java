package com.aims.logic.sdk.entity;

import com.aims.logic.sdk.annotation.TableName;
import com.aims.logic.sdk.annotation.TableId;
import com.aims.logic.sdk.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("logic_published")
public class LogicPublishedEntity extends BaseEntity {
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
    @TableField("source")
    String source;
    @TableField("target")
    String target;
}
