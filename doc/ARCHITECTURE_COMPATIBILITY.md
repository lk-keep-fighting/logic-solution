# Logic-IDE 兼容性架构设计

## 🏗️ 整体架构

```
logic-solution (父项目)
│
├── logic-runtime          # 运行时核心（无 Spring 依赖）
│   └── 依赖: GraalVM JS, FastJSON, OkHttp, Caffeine
│
├── logic-sdk              # SDK 层
│   ├── 依赖: logic-runtime
│   └── Spring 依赖: AOP, JDBC
│
├── logic-ide              # IDE 组件（本次优化重点）
│   ├── 依赖: logic-sdk
│   └── Spring 依赖: Web, WebFlux
│
├── test-suite             # 功能测试套件
│   └── 依赖: logic-ide
│
└── compatibility-tests    # 兼容性测试（新增）
    ├── spring-boot-2-tests
    └── spring-boot-3-tests
```

## 🔄 双版本构建策略

### Profile 驱动的构建

```
┌─────────────────────────────────────────────────────┐
│              Maven Profile 选择                      │
├─────────────────────────────────────────────────────┤
│                                                      │
│  spring-boot-2 (默认)    │    spring-boot-3         │
│  ├─ Spring Boot 2.7.18   │    ├─ Spring Boot 3.2.5  │
│  ├─ javax.* 命名空间     │    ├─ jakarta.* 命名空间 │
│  └─ artifact: logic-ide  │    └─ artifact: logic-ide-spring-boot-3
│                                                      │
└─────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│              构建产物                                │
├─────────────────────────────────────────────────────┤
│                                                      │
│  Spring Boot 2 版本      │    Spring Boot 3 版本     │
│  ├─ logic-runtime.jar    │    ├─ logic-runtime-spring-boot-3.jar
│  ├─ logic-sdk.jar        │    ├─ logic-sdk-spring-boot-3.jar
│  └─ logic-ide.jar        │    └─ logic-ide-spring-boot-3.jar
│                                                      │
└─────────────────────────────────────────────────────┘
```

## 📦 依赖层次结构

### Spring Boot 2 依赖链

```
logic-ide (Spring Boot 2)
│
├─ logic-sdk
│  ├─ logic-runtime (无 Spring 依赖)
│  │  ├─ GraalVM JS 22.3.5
│  │  ├─ FastJSON2 2.0.37
│  │  ├─ OkHttp 4.10.0
│  │  └─ Caffeine 2.9.2
│  │
│  ├─ Spring Boot 2.7.18
│  │  ├─ spring-boot-starter-aop
│  │  └─ spring-boot-starter-jdbc
│  │
│  ├─ MySQL Connector 8.0.33
│  └─ Redisson Spring Boot Starter 3.27.2 (optional)
│
├─ Spring Boot 2.7.18
│  ├─ spring-boot-starter-web
│  └─ spring-boot-starter-webflux
│
├─ JavaParser 3.25.10
└─ Redisson Spring Boot Starter 3.27.2 (optional)
```

### Spring Boot 3 依赖链

```
logic-ide-spring-boot-3 (Spring Boot 3)
│
├─ logic-sdk-spring-boot-3
│  ├─ logic-runtime-spring-boot-3 (无 Spring 依赖)
│  │  ├─ GraalVM JS 22.3.5
│  │  ├─ FastJSON2 2.0.37
│  │  ├─ OkHttp 4.10.0
│  │  └─ Caffeine 2.9.2
│  │
│  ├─ Spring Boot 3.2.5
│  │  ├─ spring-boot-starter-aop
│  │  └─ spring-boot-starter-jdbc
│  │
│  ├─ MySQL Connector 8.0.33
│  └─ Redisson Spring Boot Starter 3.27.2 (optional)
│     └─ 自动适配 jakarta.* 命名空间
│
├─ Spring Boot 3.2.5
│  ├─ spring-boot-starter-web
│  └─ spring-boot-starter-webflux
│
├─ JavaParser 3.25.10
└─ Redisson Spring Boot Starter 3.27.2 (optional)
   └─ 自动适配 jakarta.* 命名空间
```

## 🔑 关键设计决策

### 1. 使用 Spring Boot Starter

**决策：** 使用 `redisson-spring-boot-starter` 而不是 `redisson`

**原因：**
- ✅ 自动配置支持
- ✅ 自动适配 Spring Boot 版本
- ✅ 处理命名空间切换（javax ↔ jakarta）
- ✅ 简化配置

**对比：**
```xml
<!-- ❌ 旧方式：需要手动处理兼容性 -->
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson</artifactId>
    <version>3.15.2</version>
</dependency>

<!-- ✅ 新方式：自动适配 -->
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.27.2</version>
    <optional>true</optional>
</dependency>
```

### 2. Optional 依赖

**决策：** Redisson 设置为 `optional=true`

**原因：**
- 不是所有用户都需要 Redis 功能
- 减少强制依赖
- 用户可以按需引入

**使用方式：**
```xml
<!-- 用户项目中显式引入 -->
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.27.2</version>
</dependency>
```

### 3. Artifact 命名策略

**决策：** 使用 `${artifact.suffix}` 区分版本

**实现：**
```xml
<!-- Spring Boot 2 -->
<artifactId>logic-ide</artifactId>

<!-- Spring Boot 3 -->
<artifactId>logic-ide-spring-boot-3</artifactId>
```

**优势：**
- 清晰标识版本
- 避免依赖冲突
- 便于用户选择

## 🧪 测试架构

### 测试模块设计

```
compatibility-tests/
│
├─ spring-boot-2-tests/
│  ├─ 独立的 Spring Boot 2.7.18 环境
│  ├─ 依赖: logic-ide (Spring Boot 2 版本)
│  └─ 测试:
│     ├─ 上下文加载
│     ├─ 版本验证
│     ├─ Bean 可用性
│     └─ Web 环境
│
└─ spring-boot-3-tests/
   ├─ 独立的 Spring Boot 3.2.5 环境
   ├─ 依赖: logic-ide-spring-boot-3
   └─ 测试:
      ├─ 上下文加载
      ├─ 版本验证
      ├─ Bean 可用性
      ├─ Web 环境
      └─ Jakarta 命名空间
```

### 测试隔离

```
┌──────────────────────────────────────────────────┐
│         独立的 dependencyManagement              │
├──────────────────────────────────────────────────┤
│                                                   │
│  spring-boot-2-tests         spring-boot-3-tests │
│  ├─ Spring Boot 2.7.18       ├─ Spring Boot 3.2.5│
│  ├─ 独立的类加载器           ├─ 独立的类加载器   │
│  └─ 不会相互影响             └─ 不会相互影响     │
│                                                   │
└──────────────────────────────────────────────────┘
```

## 🔄 构建流程

### 完整构建流程

```
1. 清理
   mvn clean

2. 构建 Spring Boot 2 版本
   mvn install -P spring-boot-2
   ├─ 编译 logic-runtime
   ├─ 编译 logic-sdk
   ├─ 编译 logic-ide
   └─ 生成: logic-ide-0.10.8-SNAPSHOT.jar

3. 测试 Spring Boot 2
   cd compatibility-tests/spring-boot-2-tests
   mvn test
   └─ 验证兼容性

4. 构建 Spring Boot 3 版本
   mvn install -P spring-boot-3
   ├─ 编译 logic-runtime-spring-boot-3
   ├─ 编译 logic-sdk-spring-boot-3
   ├─ 编译 logic-ide-spring-boot-3
   └─ 生成: logic-ide-spring-boot-3-0.10.8-SNAPSHOT.jar

5. 测试 Spring Boot 3
   cd compatibility-tests/spring-boot-3-tests
   mvn test
   └─ 验证兼容性
```

### 自动化脚本流程

```bash
./test-compatibility.sh
│
├─ 步骤 1: 清理
│  └─ mvn clean
│
├─ 步骤 2: 构建 Spring Boot 2
│  └─ mvn install -P spring-boot-2 -DskipTests
│
├─ 步骤 3: 测试 Spring Boot 2
│  └─ cd compatibility-tests/spring-boot-2-tests && mvn test
│
├─ 步骤 4: 构建 Spring Boot 3
│  └─ mvn install -P spring-boot-3 -DskipTests
│
├─ 步骤 5: 测试 Spring Boot 3
│  └─ cd compatibility-tests/spring-boot-3-tests && mvn test
│
└─ 步骤 6: 汇总结果
   └─ 显示彩色测试报告
```

## 📊 版本兼容性矩阵

| 组件 | Spring Boot 2 | Spring Boot 3 | 说明 |
|------|---------------|---------------|------|
| logic-runtime | ✅ | ✅ | 无 Spring 依赖 |
| logic-sdk | ✅ | ✅ | 使用 Spring Boot Starter |
| logic-ide | ✅ | ✅ | 使用 Spring Boot Starter |
| Redisson | ✅ | ✅ | 3.27.2 支持双版本 |
| JavaParser | ✅ | ✅ | 无 Spring 依赖 |
| MySQL Connector | ✅ | ✅ | 8.0.33 支持双版本 |

## 🎯 最佳实践

### 1. 依赖管理
```xml
<!-- ✅ 好的做法：使用属性管理版本 -->
<properties>
    <redisson.version>3.27.2</redisson.version>
</properties>
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>${redisson.version}</version>
</dependency>

<!-- ❌ 不好的做法：硬编码版本 -->
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson</artifactId>
    <version>3.15.2</version>
</dependency>
```

### 2. Profile 使用
```bash
# ✅ 明确指定 Profile
mvn clean install -P spring-boot-2
mvn clean install -P spring-boot-3

# ❌ 不指定 Profile（依赖默认行为）
mvn clean install
```

### 3. 测试策略
```bash
# ✅ 发布前测试所有版本
./test-compatibility.sh

# ❌ 只测试一个版本
mvn test
```

## 🚀 未来扩展

### 支持更多版本
```xml
<profile>
    <id>spring-boot-3.3</id>
    <properties>
        <spring-boot.version>3.3.0</spring-boot.version>
        <artifact.suffix>-spring-boot-3.3</artifact.suffix>
    </properties>
</profile>
```

### 支持其他框架
- Quarkus
- Micronaut
- Helidon

### 性能优化
- 依赖瘦身
- 启动时间优化
- 内存占用优化

## 📚 参考资料

- [Maven Multi-Module Projects](https://maven.apache.org/guides/mini/guide-multiple-modules.html)
- [Spring Boot Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)
- [Redisson Documentation](https://github.com/redisson/redisson/wiki/Table-of-Content)
