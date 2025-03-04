package com.aims.plugin.demo;

import com.aims.logic.sdk.annotation.LogicItem;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Date;

@Component
public class TestComponent {

    @PostConstruct
    public void init() {
        System.out.println("动态组件 TestComponent 已加载！");
    }

    @LogicItem(name = "测试同名方法2")
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


}
