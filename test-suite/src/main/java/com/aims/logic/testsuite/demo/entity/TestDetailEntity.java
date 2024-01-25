package com.aims.logic.testsuite.demo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("test_detail")
public class TestDetailEntity {
    @TableId
    String id;
    String name;
    String testId;
}
