package com.aims.logic.service.demo;

import com.aims.logic.service.demo.dto.AllPrimitiveType;
import com.aims.logic.service.demo.dto.TestInput;
import com.aims.logic.service.demo.dto.TestInput2;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class test {
    public TestInput func1(TestInput2 input) {
        return input.getInputs().stream().max(Comparator.comparingInt(TestInput::getI)).orElse(null);
    }

    //    public TestInput func2(TestInput2 input2) {
//        return input2.getInputs().stream().max((a, b) -> a.getI() - b.getI()).orElse(null);
//    }
    public String strs(String[] strings) {
        return Arrays.stream(strings).reduce("", (x, y) -> {
            return x + "-" + y;
        });
    }

    /*
    int型数组
     */
    public int ints(int[] ints, boolean trueForSumOrMax, Boolean BoolValue) {
        return Arrays.stream(ints).reduce(0, (trueForSumOrMax | BoolValue) ? Integer::sum : Integer::max);
    }

    public Date dates(Date date) {
        return date;
    }

    public AllPrimitiveType allPrimitiveType(AllPrimitiveType values) {
        return values;
    }

    public TestInput func3(List<TestInput> input, Integer i, List<String> strs) {
        if (input != null)
            return input.stream().max(Comparator.comparingInt(TestInput::getI)).orElse(null);
        else return new TestInput().setArr(strs);
    }

//    public String func1(JSONObject obj) {
//        return obj.toJSONString();
//    }
}
