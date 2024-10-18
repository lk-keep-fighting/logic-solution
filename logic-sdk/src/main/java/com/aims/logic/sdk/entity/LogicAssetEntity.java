package com.aims.logic.sdk.entity;

import com.aims.logic.sdk.annotation.TableField;
import com.aims.logic.sdk.annotation.TableId;
import com.aims.logic.sdk.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("logic_asset")
public class LogicAssetEntity extends BaseEntity{
    @TableId
    String id;
    String code;
    String name;
    String version;
    String type;
    String config;
    @TableField("updateTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    LocalDateTime updateTime;
}
