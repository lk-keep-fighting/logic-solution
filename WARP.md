# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

Logic IDE 是一个基于 Java 17 和 Spring Boot 的可视化逻辑编排引擎，支持通过拖拽式界面设计业务流程，并提供强大的运行时执行能力。

## Module Architecture

这是一个多模块 Maven 项目，采用清晰的分层架构：

```
logic-solution/                 # 父模块
├── logic-runtime/             # 核心执行引擎 - 最底层
├── logic-sdk/                 # SDK和服务层 - 业务接口层  
├── logic-ide/                 # Web IDE界面 - 设计时界面
├── logic-ide-service/         # IDE服务端 - 独立服务版本
└── test-suite/               # 完整示例和测试套件
```

**依赖关系**：
- `logic-sdk` 依赖 `logic-runtime` (核心执行引擎)
- `logic-ide` 依赖 `logic-sdk` (提供Web界面和API)
- `test-suite` 依赖 `logic-ide` (完整示例应用)

## Core Development Commands

### Build and Compile
```bash
# 完整构建项目
mvn clean package

# 跳过测试快速构建
mvn clean package -DskipTests

# 安装到本地仓库（模块间依赖需要）
mvn clean install

# 仅构建特定模块
cd logic-sdk && mvn clean package
```

### Run the Application
```bash
# 运行完整的测试套件（包含Web IDE）
cd test-suite
mvn spring-boot:run

# 应用启动后访问：http://localhost:8888
```

### Testing
```bash
# 运行所有测试
mvn test

# 运行特定模块的测试
cd test-suite && mvn test

# 运行特定测试类
mvn test -Dtest=TestRun

# 运行单个测试方法
mvn test -Dtest=TestRun#test
```

### Development Server
```bash
# 启动开发服务器（带热重载）
cd test-suite
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=true"
```

## Configuration Structure

### Environment Configuration
- **配置目录**：`test-suite/test-case-configs/`
- **环境变量**：`envs/` 目录下存放不同环境配置
- **逻辑定义**：`logics/` 目录下存放逻辑编排配置

### Database Configuration
主要在 `test-suite/src/main/resources/application.yaml`:
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/logic_test_0_9
    username: root
    password: ${DB_PASSWORD}
```

### Logic Configuration
```yaml
logic:
  config-dir: /path/to/logic-configs  # 逻辑配置目录
  scan-package-names: com.aims.logic.testsuite  # 自动扫描组件包名
  log:
    store: es  # 日志存储：database/elasticsearch/memory
    es:
      host: http://es-host
      index: logic-log-index
```

## Core Components and APIs

### LogicRunnerService - 核心执行服务
主要执行接口位于 `logic-runtime/src/main/java/com/aims/logic/runtime/service/LogicRunnerService.java`

**无状态执行**：
```java
// JSON字符串参数
LogicRunResult result = logicService.runByJson("logic-id", "{\"key\":\"value\"}");

// Map参数（推荐）
Map<String, Object> params = Map.of("input", "hello world");
LogicRunResult result = logicService.runByMap("logic-id", params);

// 可变参数
LogicRunResult result = logicService.runByObjectArgs("logic-id", param1, param2);
```

**有状态业务实例**：
```java
// 创建或继续执行业务实例
LogicRunResult result = logicService.runBizByMap("logic-id", "biz-001", params);

// 重试失败的业务实例
LogicRunResult result = logicService.retryErrorBiz("logic-id", "biz-001");
```

### 自定义组件扩展
通过注解自动注册业务组件：
```java
@Component
@LogicItemFunction(name = "自定义组件", group = "业务组件")
public class CustomFunction implements ILogicItemFunctionRunner {
    @Override
    public LogicItemRunResult invoke(FunctionContext ctx, LogicItemTreeNode dsl) {
        // 自定义逻辑实现
        return new LogicItemRunResult().setSuccess(true).setData("result");
    }
}
```

## Development Guidelines

### Maven Repository Configuration
项目使用内部 Nexus 仓库：
```xml
<repositories>
    <repository>
        <id>nexus</id>
        <name>公司镜像仓库</name>
        <url>https://nexus.aimstek.cn/repository/maven-public</url>
    </repository>
</repositories>
```

### Version Management
- 使用 `flatten-maven-plugin` 管理版本号
- 当前版本：`0.9.4.250816-SNAPSHOT`
- 版本属性通过 `${revision}` 统一管理

### Java Version Requirements
- **Java 17+** 必需
- **Maven 3.6+** 必需
- **Spring Boot 2.7.2** 框架版本

### Testing Strategy
- 单元测试：每个模块都有独立的测试
- 集成测试：通过 `test-suite` 模块进行完整测试
- 测试覆盖：重点测试逻辑执行、并发控制、事务管理

### Database Schema
项目需要 MySQL 数据库，主要表：
- `logic_instance` - 逻辑实例表
- 日志相关表（根据配置可使用 ES 或数据库存储）

### Configuration Best Practices
- 逻辑配置文件使用 JSON 格式存储在 `config-dir` 指定目录
- 环境变量支持运行时动态设置
- 支持离线模式和在线模式两种配置管理方式

### Common Debugging
- 通过 Web IDE 界面查看逻辑执行日志
- 使用 `test-suite` 模块的测试用例进行调试
- 日志级别可通过 `LOG=on` 环境变量控制

## Integration Points

### 事务管理
支持多种事务传播机制：
- `everyRequest` - 每个请求一个事务
- 支持嵌套事务和事务组功能

### 外部系统集成
- HTTP 节点支持调用外部 API
- 数据库节点支持复杂查询和 ORM 操作
- 支持自定义数据连接扩展

### 部署模式
- 开发环境：通过 `test-suite` 运行完整功能
- 生产环境：可独立部署 `logic-ide-service` 作为服务端
- 支持 Docker 和 Kubernetes 部署

## Dependencies and Tools

### Core Dependencies
- Spring Boot 2.7.2
- GraalVM JS 引擎（用于 JavaScript 执行）
- MyBatis Plus 3.5.2
- Lombok 1.18.32

### Development Tools
- Maven Wrapper 提供版本一致性
- 支持 IDE 插件开发和调试
- 前端基于现代 Web 技术栈

此项目的核心价值在于可视化逻辑编排能力，支持复杂业务流程的图形化设计和高性能执行。
