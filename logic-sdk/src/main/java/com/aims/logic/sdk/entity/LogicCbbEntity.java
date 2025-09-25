package com.aims.logic.sdk.entity;

import com.aims.logic.sdk.annotation.TableField;
import com.aims.logic.sdk.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("logic_cbb")
public class LogicCbbEntity extends BaseEntity {
    String id;
    String name;
    String version;
    String type;
    String group;
    String order;
    @TableField("configJson")
    String configJson;
    @TableField("updateTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    LocalDateTime updateTime;
}
