package com.aims.logic.sdk.aop;


import com.aims.logic.sdk.annotation.ConditionalOnLogicLogService;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

public class LogicLogServiceCondition implements Condition {
    private static final String CONDITIONAL_ON_LOGIC_LOG_SERVICE = ConditionalOnLogicLogService.class.getName();
    private static final String ANNOTATION_ATTRIBUTE_VALUE = "value";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // 获取配置值并处理空字符串情况
        String impl = context.getEnvironment().getProperty("logic.log.store");
        if (impl == null || impl.trim().isEmpty()) {
            impl = "db";
        }

        // 安全获取注解属性
        Map<String, Object> attributes = metadata.getAnnotationAttributes(CONDITIONAL_ON_LOGIC_LOG_SERVICE);
        if (attributes == null) {
            return false;
        }

        Object requiredValue = attributes.get(ANNOTATION_ATTRIBUTE_VALUE);
        if (requiredValue == null) {
            return false; // 或者根据业务需求决定如何处理 null
        }

        return impl.equals(requiredValue.toString());
    }
}
