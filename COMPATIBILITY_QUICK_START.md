# Logic-IDE Spring Boot 兼容性快速指南

## 🎯 核心改进

### 1. 依赖优化
- ✅ Redisson 升级到 3.27.2（支持 Spring Boot 2 & 3）
- ✅ 使用 `redisson-spring-boot-starter` 自动适配
- ✅ 统一版本管理，避免硬编码
- ✅ 清理注释和重复依赖

### 2. 兼容性测试
- ✅ 独立的 Spring Boot 2 测试模块
- ✅ 独立的 Spring Boot 3 测试模块
- ✅ 自动化测试脚本

## 🚀 快速开始

### 构建和测试

```bash
# 1. 测试所有版本（推荐）
./test-compatibility.sh

# 2. 仅构建 Spring Boot 2 版本
mvn clean install -P spring-boot-2

# 3. 仅构建 Spring Boot 3 版本
mvn clean install -P spring-boot-3
```

### 使用方式

**在 Spring Boot 2 项目中：**
```xml
<dependency>
    <groupId>com.aims.logic</groupId>
    <artifactId>logic-ide</artifactId>
    <version>0.10.8-SNAPSHOT</version>
</dependency>
```

**在 Spring Boot 3 项目中：**
```xml
<dependency>
    <groupId>com.aims.logic</groupId>
    <artifactId>logic-ide-spring-boot-3</artifactId>
    <version>0.10.8-SNAPSHOT</version>
</dependency>
```

## 📋 关键变更

### logic-ide/pom.xml
- 升级 Redisson: `3.15.2` → `3.27.2`
- 使用 `redisson-spring-boot-starter` 替代 `redisson`
- 升级 JavaParser: `3.25.5` → `3.25.10`
- 添加版本属性管理

### logic-sdk/pom.xml
- 升级 Redisson: `3.15.2` → `3.27.2`
- 使用 `redisson-spring-boot-starter`
- 升级 MySQL Connector: `8.0.31` → `8.0.33`
- 添加版本属性管理

### 新增模块
```
compatibility-tests/
├── spring-boot-2-tests/    # Spring Boot 2.7.18 测试
└── spring-boot-3-tests/    # Spring Boot 3.2.5 测试
```

## 🔧 开发工作流

### 日常开发
```bash
# 默认使用 Spring Boot 2
mvn clean install
```

### 发布前检查
```bash
# 运行完整兼容性测试
./test-compatibility.sh
```

### 查看依赖
```bash
# Spring Boot 2 依赖树
mvn dependency:tree -P spring-boot-2

# Spring Boot 3 依赖树
mvn dependency:tree -P spring-boot-3
```

## 📊 测试覆盖

### 自动化测试项
- ✅ 应用上下文加载
- ✅ Spring Boot 版本验证
- ✅ Bean 注册检查
- ✅ Web 环境配置
- ✅ Jakarta EE 命名空间（Spring Boot 3）

### 手动测试建议
- 测试 Redisson 连接（如果使用）
- 测试 WebFlux 功能
- 测试自定义配置加载

## 🎓 最佳实践

### 1. 版本选择
- 新项目使用 Spring Boot 3
- 遗留项目继续使用 Spring Boot 2
- 逐步迁移，不要强制升级

### 2. 依赖管理
- 使用 Maven Profile 切换版本
- 不要在子模块中硬编码版本号
- 优先使用 Spring Boot Starter

### 3. 测试策略
- 每次发布前运行兼容性测试
- 在 CI/CD 中集成自动化测试
- 保持测试用例更新

## 📚 详细文档

查看完整文档：[doc/DEPENDENCY_OPTIMIZATION.md](doc/DEPENDENCY_OPTIMIZATION.md)

## ❓ 常见问题

### Q: 如何知道我应该使用哪个版本？
A: 检查你的项目 Spring Boot 版本：
```bash
mvn dependency:tree | grep spring-boot
```
- 2.x → 使用 `logic-ide`
- 3.x → 使用 `logic-ide-spring-boot-3`

### Q: 可以在同一个项目中混用吗？
A: 不可以。必须选择与你的 Spring Boot 版本匹配的 logic-ide 版本。

### Q: 如何升级到 Spring Boot 3？
A: 
1. 升级项目到 Spring Boot 3
2. 更新 logic-ide 依赖为 `logic-ide-spring-boot-3`
3. 处理 javax → jakarta 命名空间变更
4. 运行测试验证

### Q: Redisson 是必需的吗？
A: 不是。Redisson 是可选依赖（`optional=true`），只在需要时引入。

## 🐛 问题反馈

如遇到兼容性问题，请提供：
1. Spring Boot 版本
2. logic-ide 版本
3. 完整错误日志
4. 依赖树输出
