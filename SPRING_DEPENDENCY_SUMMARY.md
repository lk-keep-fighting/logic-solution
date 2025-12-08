# Logic-IDE Spring 依赖分析总结

## 🔍 核心发现

经过深入分析源码，发现 **logic-ide 及其所有依赖都需要 Spring Boot**：

### 依赖情况

| 模块 | Spring 依赖 | 必要性 | 说明 |
|------|-------------|--------|------|
| **logic-runtime** | ✅ 依赖 | ⚠️ 可优化 | 使用 Spring DI 和 Bean 管理 |
| **logic-sdk** | ✅ 依赖 | ✅ 必需 | 需要事务管理和 JDBC |
| **logic-ide** | ✅ 依赖 | ✅ 必需 | 提供 REST API 接口 |

## 📊 详细分析

### 1. logic-runtime（运行时核心）

**Spring 使用情况：**
- `@Component` - 8 个类
- `@Service` - 4 个函数实现类
- `@Configuration` - 2 个配置类
- `ApplicationContextAware` - 用于 Bean 查找

**关键代码：**
```java
// 函数实现类都使用 @Service
@Service
public class JsFunction implements ILogicItemFunctionRunner { }

@Service
public class HttpFunction implements ILogicItemFunctionRunner { }

// 使用 Spring 的 ApplicationContext 查找 Bean
@Component
public class FunctionServiceLocator implements ApplicationContextAware {
    // 自动发现所有函数实现
}
```

**问题：**
- ❌ 作为"运行时核心"，不应该依赖 Spring
- ❌ 无法在非 Spring 环境中使用
- ⚠️ 架构定位与实现不符

### 2. logic-sdk（SDK 层）

**Spring 使用情况：**
- `@Service` - 8 个服务类
- `@Transactional` - 事务管理
- `JdbcTemplate` - 数据库操作
- `PlatformTransactionManager` - 事务管理器

**关键代码：**
```java
@Service
public class LogicRunnerServiceImpl {
    @Transactional(propagation = Propagation.REQUIRED)
    public void execute() { }
}

@Service
public class BaseServiceImpl {
    @Autowired
    JdbcTemplate jdbcTemplate;
}
```

**评估：**
- ✅ 依赖合理，需要 Spring 的事务管理
- ✅ 需要 Spring JDBC 支持
- ✅ 业务逻辑复杂，DI 简化了管理

### 3. logic-ide（IDE 组件）

**Spring 使用情况：**
- `@RestController` - 11 个控制器
- `@Configuration` - 3 个配置类
- Spring Web MVC - 完整使用
- `WebClient` - 响应式 HTTP

**关键代码：**
```java
@RestController
public class LogicIdeController {
    @PostMapping("/api/ide/logic/save")
    public Object save(@RequestBody JSONObject json) { }
}

@Configuration
public class LogicMvcConfigurer implements WebMvcConfigurer { }
```

**评估：**
- ✅ 必须依赖 Spring Web
- ✅ 核心功能是提供 REST API
- ✅ 无法移除 Spring 依赖

## 💡 优化建议

### 方案对比

| 方案 | 重构成本 | 优势 | 劣势 | 建议 |
|------|----------|------|------|------|
| **保持现状** | 低 | 无需改动 | 架构耦合 | ✅ 短期采用 |
| **解耦 runtime** | 高 | 架构清晰 | 需要重构 | 🔄 中期考虑 |
| **创建适配层** | 中 | 平衡方案 | 增加复杂度 | 💡 长期规划 |

### 推荐方案：保持现状（短期）

**理由：**
1. ✅ 已解决 Spring Boot 2/3 兼容性问题
2. ✅ 依赖管理已规范化
3. ✅ 测试覆盖已完善
4. ✅ 功能稳定，无需大规模重构

**行动项：**
- 明确文档说明依赖关系
- 在 README 中说明运行环境要求
- 提供清晰的架构说明

## 📝 依赖声明优化

### 当前问题
父 POM 中已经声明了 Spring Boot 依赖：
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

### 建议调整

**父 POM (pom.xml):**
```xml
<dependencies>
    <!-- 所有模块都需要的基础依赖 -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.32</version>
        <scope>provided</scope>
    </dependency>
    
    <!-- 移除 spring-boot-starter，由子模块按需引入 -->
</dependencies>
```

**logic-runtime/pom.xml:**
```xml
<dependencies>
    <!-- 核心依赖 -->
    <dependency>
        <groupId>org.graalvm.js</groupId>
        <artifactId>js</artifactId>
        <version>22.3.5</version>
    </dependency>
    
    <!-- Spring 依赖（当前必需）-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

**logic-sdk/pom.xml:**
```xml
<dependencies>
    <dependency>
        <groupId>com.aims.logic</groupId>
        <artifactId>logic-runtime${artifact.suffix}</artifactId>
        <version>${logic-runtime.version}</version>
    </dependency>
    
    <!-- Spring Boot 依赖 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>
</dependencies>
```

**logic-ide/pom.xml:**
```xml
<dependencies>
    <dependency>
        <groupId>com.aims.logic</groupId>
        <artifactId>logic-sdk${artifact.suffix}</artifactId>
        <version>${logic-sdk.version}</version>
    </dependency>
    
    <!-- Spring Boot Web 依赖 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
</dependencies>
```

## 🎯 结论

### 当前状态
- ✅ **所有模块都依赖 Spring Boot**
- ✅ logic-sdk 和 logic-ide 的依赖是必需的
- ⚠️ logic-runtime 的依赖可以优化，但需要重构

### 是否可以简化？
- ❌ **不能完全移除 Spring 依赖**
- ✅ **可以优化依赖声明**（从父 POM 移到子模块）
- 🔄 **可以解耦 logic-runtime**（需要重构）

### 最终建议
1. **短期**：保持现状，优化依赖声明
2. **中期**：完善文档，明确依赖关系
3. **长期**：评估 logic-runtime 解耦的必要性

## 📚 相关文档

- 详细分析：[doc/SPRING_DEPENDENCY_ANALYSIS.md](doc/SPRING_DEPENDENCY_ANALYSIS.md)
- 兼容性方案：[COMPATIBILITY_QUICK_START.md](COMPATIBILITY_QUICK_START.md)
- 依赖优化：[doc/DEPENDENCY_OPTIMIZATION.md](doc/DEPENDENCY_OPTIMIZATION.md)
