# Logic-IDE 迁移指南

本指南帮助用户从旧版本迁移到支持 Spring Boot 2 和 3 的新版本。

## 📋 目录

- [迁移前准备](#迁移前准备)
- [Spring Boot 2 用户](#spring-boot-2-用户)
- [Spring Boot 3 用户](#spring-boot-3-用户)
- [常见问题](#常见问题)
- [回滚方案](#回滚方案)

## 🔍 迁移前准备

### 1. 检查当前 Spring Boot 版本

```bash
# 方法 1: 查看依赖树
mvn dependency:tree | grep spring-boot

# 方法 2: 查看 pom.xml
grep -A 2 "spring-boot-starter-parent" pom.xml
```

### 2. 备份当前配置

```bash
# 备份 pom.xml
cp pom.xml pom.xml.backup

# 备份依赖配置
mvn dependency:tree > dependencies-before.txt
```

### 3. 确认 logic-ide 版本

当前使用的版本：
```xml
<dependency>
    <groupId>com.aims.logic</groupId>
    <artifactId>logic-ide</artifactId>
    <version>旧版本</version>
</dependency>
```

## 🔄 Spring Boot 2 用户

### 场景：继续使用 Spring Boot 2

如果你的项目使用 Spring Boot 2.x，迁移非常简单。

#### 步骤 1: 更新依赖版本

```xml
<dependency>
    <groupId>com.aims.logic</groupId>
    <artifactId>logic-ide</artifactId>
    <version>0.10.8-SNAPSHOT</version>
</dependency>
```

#### 步骤 2: 如果使用 Redisson

**旧配置（可能需要更新）：**
```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson</artifactId>
    <version>3.15.2</version>
</dependency>
```

**新配置（推荐）：**
```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.27.2</version>
</dependency>
```

#### 步骤 3: 测试

```bash
# 清理并重新构建
mvn clean install

# 运行测试
mvn test

# 启动应用验证
mvn spring-boot:run
```

#### 步骤 4: 验证功能

- ✅ 应用正常启动
- ✅ Logic-IDE 功能正常
- ✅ Redisson 连接正常（如果使用）
- ✅ 所有业务功能正常

### 预期变化

- ✅ **无破坏性变更**
- ✅ Redisson 版本升级带来性能提升
- ✅ 更好的稳定性

## 🚀 Spring Boot 3 用户

### 场景 1: 新项目直接使用 Spring Boot 3

#### 步骤 1: 配置 Spring Boot 3

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.5</version>
</parent>
```

#### 步骤 2: 添加 logic-ide 依赖

```xml
<dependency>
    <groupId>com.aims.logic</groupId>
    <artifactId>logic-ide-spring-boot-3</artifactId>
    <version>0.10.8-SNAPSHOT</version>
</dependency>
```

#### 步骤 3: 如果使用 Redisson

```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.27.2</version>
</dependency>
```

#### 步骤 4: 测试

```bash
mvn clean install
mvn test
mvn spring-boot:run
```

### 场景 2: 从 Spring Boot 2 升级到 3

这是一个更复杂的迁移过程。

#### 步骤 1: 升级 Spring Boot

```xml
<!-- 旧版本 -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.x</version>
</parent>

<!-- 新版本 -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.5</version>
</parent>
```

#### 步骤 2: 更新 logic-ide

```xml
<!-- 旧版本 -->
<dependency>
    <groupId>com.aims.logic</groupId>
    <artifactId>logic-ide</artifactId>
    <version>旧版本</version>
</dependency>

<!-- 新版本 -->
<dependency>
    <groupId>com.aims.logic</groupId>
    <artifactId>logic-ide-spring-boot-3</artifactId>
    <version>0.10.8-SNAPSHOT</version>
</dependency>
```

#### 步骤 3: 处理命名空间变更

Spring Boot 3 使用 Jakarta EE，需要更新导入：

```java
// ❌ 旧的 javax 命名空间
import javax.servlet.http.HttpServletRequest;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

// ✅ 新的 jakarta 命名空间
import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
```

**自动化工具：**
```bash
# 使用 OpenRewrite 自动迁移
mvn org.openrewrite.maven:rewrite-maven-plugin:run \
  -Drewrite.activeRecipes=org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0
```

#### 步骤 4: 更新其他依赖

检查并更新其他依赖到支持 Spring Boot 3 的版本：

```bash
# 查看依赖冲突
mvn dependency:tree

# 分析依赖问题
mvn dependency:analyze
```

#### 步骤 5: 测试

```bash
# 清理并重新构建
mvn clean install

# 运行测试
mvn test

# 检查是否有编译错误
mvn compile
```

#### 步骤 6: 逐步验证

1. **单元测试**
   ```bash
   mvn test
   ```

2. **集成测试**
   ```bash
   mvn verify
   ```

3. **启动应用**
   ```bash
   mvn spring-boot:run
   ```

4. **功能测试**
   - 测试所有关键业务功能
   - 验证 Logic-IDE 功能
   - 检查日志是否有警告或错误

## ⚠️ 常见问题

### 问题 1: 依赖冲突

**症状：**
```
[ERROR] Failed to execute goal ... : Unresolvable build extension
```

**解决方案：**
```bash
# 清理本地仓库
rm -rf ~/.m2/repository/com/aims/logic

# 重新构建
mvn clean install -U
```

### 问题 2: 命名空间错误（Spring Boot 3）

**症状：**
```
[ERROR] package javax.servlet does not exist
```

**解决方案：**
```bash
# 使用 IDE 的查找替换功能
javax.servlet → jakarta.servlet
javax.persistence → jakarta.persistence
javax.validation → jakarta.validation
```

### 问题 3: Redisson 连接失败

**症状：**
```
Unable to connect to Redis
```

**解决方案：**

1. 检查 Redisson 配置：
```yaml
spring:
  redis:
    host: localhost
    port: 6379
```

2. 确认使用正确的 starter：
```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.27.2</version>
</dependency>
```

### 问题 4: 版本选择错误

**症状：**
```
NoSuchMethodError or ClassNotFoundException
```

**解决方案：**

确认使用正确的 artifact：
- Spring Boot 2 → `logic-ide`
- Spring Boot 3 → `logic-ide-spring-boot-3`

```bash
# 检查实际使用的版本
mvn dependency:tree | grep logic-ide
```

### 问题 5: 测试失败

**症状：**
```
Tests in error: ...
```

**解决方案：**

1. 更新测试依赖：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

2. 更新测试注解（Spring Boot 3）：
```java
// ✅ 使用新的测试注解
@SpringBootTest
class MyTest {
    // ...
}
```

## 🔙 回滚方案

如果迁移遇到问题，可以快速回滚：

### 步骤 1: 恢复备份

```bash
# 恢复 pom.xml
cp pom.xml.backup pom.xml

# 清理构建
mvn clean
```

### 步骤 2: 重新构建

```bash
mvn clean install
```

### 步骤 3: 验证

```bash
mvn test
mvn spring-boot:run
```

## ✅ 迁移检查清单

### Spring Boot 2 迁移

- [ ] 更新 logic-ide 版本
- [ ] 更新 Redisson（如果使用）
- [ ] 运行 `mvn clean install`
- [ ] 运行 `mvn test`
- [ ] 启动应用验证
- [ ] 测试关键功能
- [ ] 检查日志无错误

### Spring Boot 3 迁移

- [ ] 升级 Spring Boot 到 3.x
- [ ] 更新为 `logic-ide-spring-boot-3`
- [ ] 更新命名空间（javax → jakarta）
- [ ] 更新其他依赖
- [ ] 运行 `mvn clean install`
- [ ] 运行 `mvn test`
- [ ] 启动应用验证
- [ ] 测试关键功能
- [ ] 性能测试
- [ ] 检查日志无错误

## 📞 获取帮助

如果遇到问题：

1. **查看文档**
   - [COMPATIBILITY_QUICK_START.md](../COMPATIBILITY_QUICK_START.md)
   - [DEPENDENCY_OPTIMIZATION.md](DEPENDENCY_OPTIMIZATION.md)

2. **运行诊断**
   ```bash
   mvn dependency:tree > dependencies.txt
   mvn help:effective-pom > effective-pom.xml
   ```

3. **联系支持**
   - 提供 Spring Boot 版本
   - 提供 logic-ide 版本
   - 提供错误日志
   - 提供依赖树

## 📚 参考资料

- [Spring Boot 3 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Jakarta EE Migration](https://jakarta.ee/resources/migration/)
- [Redisson Documentation](https://github.com/redisson/redisson/wiki)
