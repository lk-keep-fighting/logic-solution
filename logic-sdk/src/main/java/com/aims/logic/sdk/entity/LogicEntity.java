package com.aims.logic.sdk.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;

@Data
@TableName("logic")
public class LogicEntity extends Model<LogicEntity> {
    @TableId
    String id;
    String name;
    String version;
    String module;
    String configJson;
    Date updateTime;
}
