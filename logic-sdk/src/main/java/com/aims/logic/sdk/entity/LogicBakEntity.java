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
@TableName("logic_bak")
public class LogicBakEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    String aid;
    String id;
    String name;
    String version;
    String module;
    @TableField("configJson")
    String configJson;
    @TableField("updateTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    LocalDateTime updateTime;
}
