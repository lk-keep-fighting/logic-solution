package com.aims.logic.testsuite.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 所有原始类型
 */
//@Data
public class AllPrimitiveType {
    public char charValue;
    public int intValue;
    public Integer integerValue;
    public boolean booleanValue;
    public Boolean BooleanValue;
    public short shortValue;
    public Short ShortValue;
    public long longValue;
    public Long LongValue;
    public float floatValue;
    public Float FloatValue;
    public double doubleValue;
    public Double DoubleValue;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    public Date dateValue;
}
