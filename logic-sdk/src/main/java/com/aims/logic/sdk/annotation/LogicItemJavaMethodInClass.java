package com.aims.logic.sdk.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogicItemJavaMethodInClass {
    String name() default "未命名";

    String type() default "java";

    String group() default "java";
}
