package com.aims.logic.testsuite.demo;

import com.aims.logic.testsuite.demo.dto.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Component
public class testTypes {
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

    public String strs() {
        return "测试同名方法2";
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

    public String parametricType(ParametricType<TestInput> inputParametricType) {
        return inputParametricType.getValue().getStr();
    }

    public String enumType(EnumType enumType) {
        return enumType.getValue();
    }

//    public String func1(JSONObject obj) {
//        return obj.toJSONString();
//    }
}
