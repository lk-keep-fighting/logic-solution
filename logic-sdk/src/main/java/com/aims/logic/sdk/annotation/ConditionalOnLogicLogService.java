package com.aims.logic.sdk.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConditionalOnLogicLogService {
    String value() default "";
}
