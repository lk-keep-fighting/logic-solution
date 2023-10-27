package com.aims.logic.sdk.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;

@Data
@TableName("logic_published")
public class LogicPublishedEntity extends Model<LogicPublishedEntity> {
    @TableId
    long id;
    String logicId;
    String name;
    String version;
    String module;
    String configJson;
    Date publishedTime;
}
