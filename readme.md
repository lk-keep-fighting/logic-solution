# Logic IDE - 可视化逻辑编排引擎

[![Java CI](https://github.com/logic-ide/logic-solution/workflows/Java%20CI/badge.svg)](https://github.com/logic-ide/logic-solution/actions)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.logic-ide/logic-solution/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.logic-ide/logic-solution)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## 项目介绍

Logic IDE 是一个基于 Java 17 和 Spring Boot 的可视化逻辑编排引擎，支持通过拖拽式界面设计业务流程，并提供强大的运行时执行能力。

### 核心特性

- 🎨 **可视化编排**：拖拽式界面，所见即所得的逻辑设计
- 🚀 **高性能执行**：基于 GraalVM JS 引擎，支持 JavaScript、Java 混合执行  
- 🔄 **状态管理**：支持有状态业务实例，可暂停/恢复执行
- 🔐 **事务支持**：灵活的事务边界控制，支持多种事务传播机制
- 📊 **完整日志**：详细的执行日志和可视化调试界面
- 🌐 **REST API**：完整的 REST 接口，易于集成
- 🔧 **插件化**：支持自定义节点类型和函数扩展

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 5.7+ (可选)
- Redis (可选)

### 运行示例

```bash
# 克隆项目
git clone https://github.com/logic-ide/logic-solution.git
cd logic-solution

# 编译
mvn clean package

# 运行测试套件（包含 Web IDE）
cd test-suite
mvn spring-boot:run
```

访问 http://localhost:8080 查看管理界面。

### Maven 依赖

```xml
<dependency>
    <groupId>io.github.logic-ide</groupId>
    <artifactId>logic-sdk</artifactId>
    <version>0.9.4-SNAPSHOT</version>
</dependency>
```

## 模块结构

```
logic-solution/
├── logic-runtime/     # 核心执行引擎
├── logic-sdk/         # SDK 和服务层
├── logic-ide/         # Web IDE 界面
├── logic-ide-service/ # IDE 服务端
└── test-suite/        # 完整示例和测试
```

## 使用示例

### 1. 无状态执行

```java
@Autowired
private LogicRunnerService logicService;

// 执行逻辑，传入参数
Map<String, Object> params = Map.of("input", "hello world");
LogicRunResult result = logicService.runByMap("my-logic-id", params);

// 获取结果
if (result.isSuccess()) {
    System.out.println("Result: " + result.getData());
}
```

### 2. 有状态业务实例

```java
// 创建业务实例并执行
LogicRunResult result = logicService.runBizByMap("workflow-id", "biz-001", params);

// 后续从断点继续执行
LogicRunResult nextResult = logicService.runBizByMap("workflow-id", "biz-001", newParams);
```

## 配置说明

### 基础配置 (application.yml)

```yaml
logic:
  config-dir: ./logic-configs  # 逻辑配置文件目录
  log-service: database        # 日志存储：database/elasticsearch/memory
  default-tran-scope: everyRequest  # 默认事务范围
```

### 环境变量配置

```json
{
  "NODE_ENV": "development",
  "LOG": "on",
  "DEFAULT_TRAN_SCOPE": "everyRequest"
}
```

## 开发指南

### 自定义节点类型

```java
@Component
public class CustomFunction implements ILogicItemFunctionRunner {
    
    @Override
    public LogicItemRunResult invoke(FunctionContext ctx, LogicItemTreeNode dsl) {
        // 自定义逻辑实现
        return new LogicItemRunResult().setSuccess(true).setData("custom result");
    }
}
```

### 扩展数据连接

通过实现 `LogicDataService` 接口支持自定义数据源：

```java
@Service
public class CustomDataService implements LogicDataService {
    
    @Override
    public Object queryForObject(String sql, Map<String, Object> params) {
        // 自定义查询实现
        return null;
    }
}
```

## 部署指南

### Docker 部署

```dockerfile
FROM openjdk:17-jdk-slim

COPY target/logic-solution-*.jar app.jar
COPY logic-configs ./logic-configs

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Kubernetes 部署

参考 `deploy/` 目录下的 Kubernetes 配置文件。

## 贡献指南

我们欢迎所有形式的贡献！请阅读 [CONTRIBUTING.md](CONTRIBUTING.md) 了解详细信息。

### 开发环境设置

1. Fork 本项目
2. 创建特性分支：`git checkout -b feature/new-feature`
3. 提交更改：`git commit -am 'Add new feature'`
4. 推送分支：`git push origin feature/new-feature`
5. 创建 Pull Request

### 代码规范

- 遵循阿里巴巴 Java 开发手册
- 使用 4 空格缩进
- 方法和类添加必要注释
- 单元测试覆盖率 > 70%

## 许可证

本项目基于 Apache 2.0 许可证开源 - 详见 [LICENSE](LICENSE) 文件。

## 支持与反馈

- 🐛 **Bug 报告**：[GitHub Issues](https://github.com/logic-ide/logic-solution/issues)
- 💡 **功能建议**：[GitHub Discussions](https://github.com/logic-ide/logic-solution/discussions)
- 📚 **文档**：[Wiki](https://github.com/logic-ide/logic-solution/wiki)

## 更新日志

详见 [CHANGELOG.md](CHANGELOG.md)。

---

**如果这个项目对你有帮助，请给我们一个 ⭐️！**
