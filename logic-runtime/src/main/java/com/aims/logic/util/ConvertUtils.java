package com.aims.logic.util;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConvertUtils {

    public static <T> List<T> convertToObjectList(Object[] objectArray, Class<T> clazz) {
        List<T> resultList = new ArrayList<>();

        for (Object obj : objectArray) {
            T newObj = BeanUtils.instantiateClass(clazz);
            BeanWrapper beanWrapper = new BeanWrapperImpl(newObj);
            BeanUtils.copyProperties(obj, newObj, Arrays.toString(beanWrapper.getPropertyDescriptors()));
            resultList.add(newObj);
        }

        return resultList;
    }
}
