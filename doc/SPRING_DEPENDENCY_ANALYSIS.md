# Logic-IDE Spring 依赖深度分析

## 📊 依赖使用情况总结

### logic-runtime（运行时核心）

**Spring 依赖使用：**
- ✅ `@Component` - 8 个类
- ✅ `@Service` - 4 个类（函数实现）
- ✅ `@Configuration` - 2 个类
- ✅ `@Bean` - 2 个方法
- ✅ `ApplicationContextAware` - 2 个类
- ✅ `@Value` - 1 个类
- ✅ `BeanUtils` - 工具类使用

**关键发现：**
- ❌ **logic-runtime 严重依赖 Spring**
- ❌ 使用 Spring 的依赖注入和 Bean 管理
- ❌ 使用 ApplicationContext 进行 Bean 查找
- ⚠️ 这与"运行时核心"的定位不符

### logic-sdk（SDK 层）

**Spring 依赖使用：**
- ✅ `@Service` - 8 个服务类
- ✅ `@Component` - 5 个组件类
- ✅ `@Configuration` - 2 个配置类
- ✅ `@Autowired` - 大量使用
- ✅ `JdbcTemplate` - 数据库操作
- ✅ `@Transactional` - 事务管理
- ✅ `PlatformTransactionManager` - 事务管理器

**关键发现：**
- ✅ **logic-sdk 合理依赖 Spring**
- ✅ 需要 Spring 的事务管理
- ✅ 需要 Spring 的 JDBC 支持
- ✅ 需要 Spring 的依赖注入

### logic-ide（IDE 组件）

**Spring 依赖使用：**
- ✅ `@RestController` - 11 个控制器
- ✅ `@Configuration` - 3 个配置类
- ✅ `@Component` - 2 个组件
- ✅ `@Service` - 1 个服务
- ✅ `@Bean` - 1 个方法
- ✅ Spring Web MVC - 完整使用
- ✅ `RestTemplate` - HTTP 客户端
- ✅ `WebClient` - 响应式 HTTP 客户端

**关键发现：**
- ✅ **logic-ide 必须依赖 Spring Web**
- ✅ 提供 REST API 接口
- ✅ 需要 Spring MVC 功能
- ✅ 这是 IDE 组件的核心功能

## 🎯 依赖必要性分析

### 1. logic-runtime 的 Spring 依赖

#### 当前使用场景

**依赖注入和 Bean 管理：**
```java
// FunctionServiceLocator.java
@Component
public class FunctionServiceLocator implements ApplicationContextAware {
    // 用于自动发现和注册所有 ILogicItemFunctionRunner 实现
}

// SpringContextUtil.java
@Component
public class SpringContextUtil implements ApplicationContextAware {
    // 提供静态方法获取 Spring Bean
}
```

**函数实现类：**
```java
@Service
public class JsFunction implements ILogicItemFunctionRunner { }

@Service
public class HttpFunction implements ILogicItemFunctionRunner { }

@Service
public class JavaCodeFunction implements ILogicItemFunctionRunner { }

@Service
public class SwitchFunction implements ILogicItemFunctionRunner { }
```

**配置类：**
```java
@Configuration
public class GraalvmEngineConfig {
    @Bean
    public Engine graalEngine() { }
}
```

#### 问题分析

❌ **问题 1: 架构定位不清晰**
- logic-runtime 应该是纯运行时核心
- 不应该依赖 Spring 框架
- 应该可以在非 Spring 环境中使用

❌ **问题 2: 过度使用依赖注入**
- 函数实现类使用 `@Service` 注解
- 依赖 Spring 的 Bean 扫描机制
- 增加了框架耦合度

❌ **问题 3: 配置管理耦合**
- 使用 `@Configuration` 和 `@Bean`
- 配置与 Spring 强绑定
- 难以在其他环境中复用

### 2. logic-sdk 的 Spring 依赖

#### 当前使用场景

**数据库操作：**
```java
@Service
public class BaseServiceImpl {
    @Autowired
    JdbcTemplate jdbcTemplate;
    // 使用 JdbcTemplate 进行数据库操作
}
```

**事务管理：**
```java
@Service
public class LogicRunnerServiceImpl {
    @Transactional(propagation = Propagation.REQUIRED)
    public void execute() { }
}

@Component
public class TransactionalUtils {
    @Autowired
    private PlatformTransactionManager transactionManager;
}
```

**业务服务：**
```java
@Service
public class LogicServiceImpl { }

@Service
public class LogicInstanceServiceImpl { }

@Service
public class LogicDataServiceImpl { }
```

#### 必要性评估

✅ **合理依赖 1: 数据库操作**
- 需要 `spring-boot-starter-jdbc`
- JdbcTemplate 提供便捷的数据库访问
- 可以考虑使用更轻量的方案

✅ **合理依赖 2: 事务管理**
- 需要 `spring-boot-starter-aop`
- 声明式事务管理
- 这是 Spring 的核心优势

✅ **合理依赖 3: 依赖注入**
- 服务之间的依赖关系复杂
- Spring DI 简化了对象管理
- 提高了可测试性

### 3. logic-ide 的 Spring 依赖

#### 当前使用场景

**REST API 控制器：**
```java
@RestController
public class LogicIdeController {
    @PostMapping("/api/ide/logic/save")
    public Object save(@RequestBody JSONObject json) { }
}
```

**Web 配置：**
```java
@Configuration
public class LogicMvcConfigurer implements WebMvcConfigurer {
    // 配置静态资源、视图控制器等
}

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() { }
}
```

#### 必要性评估

✅ **必须依赖 1: Spring Web MVC**
- logic-ide 的核心功能是提供 Web API
- 需要 `spring-boot-starter-web`
- 无法移除

✅ **必须依赖 2: Spring WebFlux**
- 用于 AI 接口的流式响应
- 需要 `spring-boot-starter-webflux`
- 提供响应式编程支持

✅ **必须依赖 3: Spring 配置**
- Web 应用需要配置管理
- 需要 `@Configuration` 和 `@Bean`
- 这是标准做法

## 💡 优化建议

### 方案 1: 解耦 logic-runtime（推荐）

#### 目标
将 logic-runtime 改造为无 Spring 依赖的纯 Java 库

#### 具体步骤

**1. 移除 Spring 注解**
```java
// 当前
@Service
public class JsFunction implements ILogicItemFunctionRunner { }

// 改造后
public class JsFunction implements ILogicItemFunctionRunner { }
```

**2. 使用 SPI 机制替代 Spring Bean 扫描**
```java
// 创建 META-INF/services/com.aims.logic.runtime.runner.functions.ILogicItemFunctionRunner
com.aims.logic.runtime.runner.functions.impl.JsFunction
com.aims.logic.runtime.runner.functions.impl.HttpFunction
com.aims.logic.runtime.runner.functions.impl.JavaCodeFunction
com.aims.logic.runtime.runner.functions.impl.SwitchFunction
```

**3. 手动管理 Bean**
```java
public class FunctionRegistry {
    private static final Map<String, ILogicItemFunctionRunner> functions = new HashMap<>();
    
    static {
        // 使用 ServiceLoader 加载
        ServiceLoader<ILogicItemFunctionRunner> loader = 
            ServiceLoader.load(ILogicItemFunctionRunner.class);
        for (ILogicItemFunctionRunner function : loader) {
            functions.put(function.getType(), function);
        }
    }
    
    public static ILogicItemFunctionRunner getFunction(String type) {
        return functions.get(type);
    }
}
```

**4. 配置管理改造**
```java
// 当前
@Configuration
public class GraalvmEngineConfig {
    @Bean
    public Engine graalEngine() { }
}

// 改造后
public class EngineFactory {
    private static Engine instance;
    
    public static synchronized Engine getInstance() {
        if (instance == null) {
            instance = Engine.newBuilder()
                .option("engine.WarnInterpreterOnly", "false")
                .build();
        }
        return instance;
    }
}
```

#### 优势
- ✅ logic-runtime 可以独立使用
- ✅ 减少依赖，提高可移植性
- ✅ 可以在非 Spring 环境中使用
- ✅ 更清晰的架构分层

#### 劣势
- ⚠️ 需要重构现有代码
- ⚠️ 需要手动管理对象生命周期
- ⚠️ 失去 Spring 的便利性

### 方案 2: 保持现状，明确依赖（简单）

#### 目标
接受 logic-runtime 依赖 Spring 的现实，但明确文档说明

#### 具体步骤

**1. 更新 README**
```markdown
## 依赖说明

### logic-runtime
- **依赖 Spring Framework**
- 使用 Spring 的依赖注入和 Bean 管理
- 需要在 Spring 环境中运行

### logic-sdk
- **依赖 Spring Boot**
- 需要 Spring JDBC 和 AOP 支持
- 提供事务管理功能

### logic-ide
- **依赖 Spring Boot Web**
- 提供 REST API 接口
- 需要 Web 容器环境
```

**2. 优化依赖声明**
```xml
<!-- logic-runtime/pom.xml -->
<dependencies>
    <!-- 明确声明 Spring 依赖 -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-beans</artifactId>
    </dependency>
</dependencies>
```

#### 优势
- ✅ 无需重构代码
- ✅ 保持现有功能
- ✅ 快速实施

#### 劣势
- ❌ 架构耦合度高
- ❌ 无法在非 Spring 环境使用
- ❌ 增加了依赖复杂度

### 方案 3: 创建适配层（平衡）

#### 目标
保持 logic-runtime 核心无 Spring 依赖，通过适配层支持 Spring

#### 架构设计

```
logic-runtime-core (无 Spring 依赖)
    ├── 核心运行时逻辑
    ├── 函数接口定义
    └── 基础工具类

logic-runtime-spring (Spring 适配层)
    ├── Spring Bean 配置
    ├── Spring 集成支持
    └── 依赖 logic-runtime-core

logic-sdk (依赖 logic-runtime-spring)
logic-ide (依赖 logic-sdk)
```

#### 具体实现

**logic-runtime-core/pom.xml**
```xml
<dependencies>
    <!-- 无 Spring 依赖 -->
    <dependency>
        <groupId>org.graalvm.js</groupId>
        <artifactId>js</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba.fastjson2</groupId>
        <artifactId>fastjson2</artifactId>
    </dependency>
</dependencies>
```

**logic-runtime-spring/pom.xml**
```xml
<dependencies>
    <dependency>
        <groupId>com.aims.logic</groupId>
        <artifactId>logic-runtime-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

#### 优势
- ✅ 核心逻辑无 Spring 依赖
- ✅ 支持 Spring 集成
- ✅ 架构清晰，职责分明
- ✅ 可以支持其他框架

#### 劣势
- ⚠️ 需要拆分模块
- ⚠️ 增加了项目复杂度
- ⚠️ 需要维护两个模块

## 📊 依赖优化对比

| 方案 | 重构成本 | 架构清晰度 | 可移植性 | Spring Boot 2/3 兼容性 |
|------|----------|------------|----------|------------------------|
| 方案 1: 完全解耦 | 高 | 优秀 | 优秀 | 无影响 |
| 方案 2: 保持现状 | 低 | 一般 | 差 | 已解决 |
| 方案 3: 适配层 | 中 | 良好 | 良好 | 已解决 |

## 🎯 最终建议

### 短期（当前版本）

**采用方案 2: 保持现状**
- ✅ 已经解决了 Spring Boot 2/3 兼容性问题
- ✅ 依赖管理已经规范化
- ✅ 测试覆盖已经完善
- ✅ 无需大规模重构

**具体行动：**
1. 明确文档说明依赖关系
2. 在 README 中说明运行环境要求
3. 提供清晰的依赖树文档

### 中期（下一个大版本）

**考虑方案 3: 创建适配层**
- 拆分 logic-runtime 为 core 和 spring 两个模块
- 保持向后兼容
- 逐步迁移

### 长期（未来规划）

**评估方案 1: 完全解耦**
- 如果有非 Spring 环境的需求
- 如果需要支持其他框架（Quarkus, Micronaut）
- 进行完整的架构重构

## 📝 依赖声明建议

### logic-runtime/pom.xml

```xml
<dependencies>
    <!-- 核心依赖 -->
    <dependency>
        <groupId>org.graalvm.js</groupId>
        <artifactId>js</artifactId>
        <version>22.3.5</version>
    </dependency>
    <dependency>
        <groupId>com.alibaba.fastjson2</groupId>
        <artifactId>fastjson2</artifactId>
        <version>2.0.37</version>
    </dependency>
    <dependency>
        <groupId>com.squareup.okhttp3</groupId>
        <artifactId>okhttp</artifactId>
        <version>4.10.0</version>
    </dependency>
    
    <!-- Spring 依赖（当前必需）-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
        <scope>provided</scope>
        <optional>true</optional>
    </dependency>
</dependencies>
```

**说明：**
- 使用 `provided` scope 表示由使用方提供
- 使用 `optional` 表示可选依赖
- 但实际上当前版本仍然需要

### logic-sdk/pom.xml

```xml
<dependencies>
    <!-- 内部依赖 -->
    <dependency>
        <groupId>com.aims.logic</groupId>
        <artifactId>logic-runtime${artifact.suffix}</artifactId>
        <version>${logic-runtime.version}</version>
    </dependency>
    
    <!-- Spring Boot 依赖（必需）-->
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

### logic-ide/pom.xml

```xml
<dependencies>
    <!-- 内部依赖 -->
    <dependency>
        <groupId>com.aims.logic</groupId>
        <artifactId>logic-sdk${artifact.suffix}</artifactId>
        <version>${logic-sdk.version}</version>
    </dependency>
    
    <!-- Spring Boot Web 依赖（必需）-->
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

## 📚 总结

### 当前状态
- ✅ logic-runtime **依赖** Spring（使用 DI、Bean 管理）
- ✅ logic-sdk **必须依赖** Spring（事务、JDBC）
- ✅ logic-ide **必须依赖** Spring Web（REST API）

### 优化空间
- 🔄 logic-runtime 可以解耦，但需要重构
- ✅ logic-sdk 依赖合理，无需改动
- ✅ logic-ide 依赖必要，无需改动

### 建议
- **短期**: 保持现状，完善文档
- **中期**: 考虑拆分 logic-runtime
- **长期**: 评估完全解耦的可行性
