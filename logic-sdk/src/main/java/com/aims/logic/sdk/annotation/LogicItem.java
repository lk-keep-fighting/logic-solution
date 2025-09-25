package com.aims.logic.sdk.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogicItem {
    /**
     *  默认值根据group与name的全拼
     *  格式 group拼音_name拼音
     * @return
     */
    String id() default "";
    String name() default "未命名";

    String type() default "java";

    String group() default "java";

    String version() default "";

    String memo() default "";

    String shape() default "";

    String order() default "";
}
