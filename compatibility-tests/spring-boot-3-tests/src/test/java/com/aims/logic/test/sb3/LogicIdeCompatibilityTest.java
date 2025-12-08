package com.aims.logic.test.sb3;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Logic-IDE Spring Boot 3 兼容性测试
 */
@SpringBootTest
class LogicIdeCompatibilityTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext, "应用上下文应该成功加载");
    }

    @Test
    void testSpringBootVersion() {
        String version = org.springframework.boot.SpringBootVersion.getVersion();
        assertNotNull(version, "Spring Boot 版本不应为空");
        assertTrue(version.startsWith("3."), "应该是 Spring Boot 3.x 版本，实际版本: " + version);
        System.out.println("Spring Boot 版本: " + version);
    }

    @Test
    void testLogicIdeBeansAvailable() {
        // 测试 logic-ide 的核心 Bean 是否可用
        // 根据实际的 Bean 名称调整
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        assertTrue(beanNames.length > 0, "应该有 Bean 被注册");
        System.out.println("已注册的 Bean 数量: " + beanNames.length);
    }

    @Test
    void testWebEnvironment() {
        String[] profiles = applicationContext.getEnvironment().getActiveProfiles();
        assertNotNull(profiles, "环境配置应该可用");
        System.out.println("激活的 Profile: " + String.join(", ", profiles));
    }

    @Test
    void testJakartaNamespace() {
        // Spring Boot 3 使用 Jakarta EE 命名空间
        try {
            Class.forName("jakarta.servlet.http.HttpServletRequest");
            System.out.println("✓ Jakarta EE 命名空间可用");
        } catch (ClassNotFoundException e) {
            fail("Spring Boot 3 应该使用 Jakarta EE 命名空间");
        }
    }
}
