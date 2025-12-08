# Logic-IDE 兼容性测试套件

本模块用于测试 logic-ide 组件在 Spring Boot 2 和 Spring Boot 3 环境下的兼容性。

## 📁 模块结构

```
compatibility-tests/
├── spring-boot-2-tests/    # Spring Boot 2.x 兼容性测试
└── spring-boot-3-tests/    # Spring Boot 3.x 兼容性测试
```

## 🚀 运行测试

### 测试 Spring Boot 2 兼容性

```bash
# 使用 Spring Boot 2 profile 构建
mvn clean install -P spring-boot-2

# 运行 Spring Boot 2 测试
cd compatibility-tests/spring-boot-2-tests
mvn test
```

### 测试 Spring Boot 3 兼容性

```bash
# 使用 Spring Boot 3 profile 构建
mvn clean install -P spring-boot-3

# 运行 Spring Boot 3 测试
cd compatibility-tests/spring-boot-3-tests
mvn test
```

### 同时测试两个版本

```bash
# 在项目根目录执行
./test-compatibility.sh
```

## 📋 测试内容

### 基础兼容性测试
- ✅ Spring 应用上下文加载
- ✅ Spring Boot 版本验证
- ✅ Bean 注册和依赖注入
- ✅ Web 环境配置

### Spring Boot 3 特定测试
- ✅ Jakarta EE 命名空间验证
- ✅ 新 API 兼容性

## 🔧 添加自定义测试

在对应的测试模块中添加测试类：

**Spring Boot 2:**
```
spring-boot-2-tests/src/test/java/com/aims/logic/test/sb2/
```

**Spring Boot 3:**
```
spring-boot-3-tests/src/test/java/com/aims/logic/test/sb3/
```

## 📊 测试报告

测试报告生成在各自模块的 `target/surefire-reports/` 目录下。

## ⚠️ 注意事项

1. 确保先构建对应版本的 logic-ide 组件
2. Spring Boot 2 使用 `javax.*` 命名空间
3. Spring Boot 3 使用 `jakarta.*` 命名空间
4. 测试模块不会被发布到 Maven 仓库
