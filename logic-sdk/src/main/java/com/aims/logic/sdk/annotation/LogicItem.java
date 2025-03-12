package com.aims.logic.sdk.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogicItem {
    String name() default "未命名";

    String type() default "java";

    String group() default "java";
    String memo() default "";

    String shape() default "";

    String order() default "";
}
