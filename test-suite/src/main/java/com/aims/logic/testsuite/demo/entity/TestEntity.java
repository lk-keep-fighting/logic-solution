package com.aims.logic.testsuite.demo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("test")
public class TestEntity {
    @TableId
    String id;
    String name;
}
