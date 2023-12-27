package com.aims.logic.sdk.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface LogicItem {
    String name() default "";

    String group() default "java";
}
