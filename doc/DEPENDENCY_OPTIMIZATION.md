# Logic-IDE 依赖优化与兼容性方案

## 📊 依赖分析总结

### 当前问题

1. **Redisson 版本过旧** (3.15.2)
   - 对 Spring Boot 3 支持不完整
   - 缺少 Jakarta EE 命名空间支持

2. **依赖版本硬编码**
   - 版本号分散在各个模块中
   - 难以统一管理和升级

3. **测试覆盖不足**
   - 缺少针对不同 Spring Boot 版本的测试
   - 无法验证兼容性

## ✅ 优化方案

### 1. 依赖版本统一管理

**父 POM 统一管理 Spring Boot 版本：**
```xml
<profiles>
    <profile>
        <id>spring-boot-2</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <properties>
            <spring-boot.version>2.7.18</spring-boot.version>
            <artifact.suffix></artifact.suffix>
        </properties>
    </profile>
    <profile>
        <id>spring-boot-3</id>
        <properties>
            <spring-boot.version>3.2.5</spring-boot.version>
            <artifact.suffix>-spring-boot-3</artifact.suffix>
        </properties>
    </profile>
</profiles>
```

### 2. 关键依赖升级

| 依赖 | 旧版本 | 新版本 | 说明 |
|------|--------|--------|------|
| Redisson | 3.15.2 | 3.27.2 | 完整支持 Spring Boot 2 & 3 |
| JavaParser | 3.25.5 | 3.25.10 | Bug 修复和改进 |
| MySQL Connector | 8.0.31 | 8.0.33 | 安全更新 |

### 3. 依赖简化原则

#### ✅ 保留的依赖
- **Spring Boot Starters**: 由父 POM 统一管理版本
- **JavaParser**: 无 Spring 依赖，版本兼容
- **Redisson**: 升级到支持双版本的 starter
- **内部依赖**: logic-runtime, logic-sdk

#### ❌ 移除的依赖
- 注释掉的依赖
- 重复的依赖声明

### 4. 兼容性策略

#### 使用 `optional=true`
对于可选功能（如 Redisson），使用 optional 依赖：
```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>${redisson.version}</version>
    <optional>true</optional>
</dependency>
```

#### 使用 Spring Boot Starter
优先使用 `-spring-boot-starter` 版本，自动适配 Spring Boot 版本：
- `redisson-spring-boot-starter` 而不是 `redisson`
- 自动处理 javax/jakarta 命名空间切换

## 🏗️ 构建和发布策略

### 构建两个版本

**Spring Boot 2 版本：**
```bash
mvn clean install -P spring-boot-2
```
生成产物：
- `logic-ide-0.10.8-SNAPSHOT.jar`
- `logic-sdk-0.10.8-SNAPSHOT.jar`
- `logic-runtime-0.10.8-SNAPSHOT.jar`

**Spring Boot 3 版本：**
```bash
mvn clean install -P spring-boot-3
```
生成产物：
- `logic-ide-spring-boot-3-0.10.8-SNAPSHOT.jar`
- `logic-sdk-spring-boot-3-0.10.8-SNAPSHOT.jar`
- `logic-runtime-spring-boot-3-0.10.8-SNAPSHOT.jar`

### 使用方式

**Spring Boot 2 项目：**
```xml
<dependency>
    <groupId>com.aims.logic</groupId>
    <artifactId>logic-ide</artifactId>
    <version>0.10.8-SNAPSHOT</version>
</dependency>
```

**Spring Boot 3 项目：**
```xml
<dependency>
    <groupId>com.aims.logic</groupId>
    <artifactId>logic-ide-spring-boot-3</artifactId>
    <version>0.10.8-SNAPSHOT</version>
</dependency>
```

## 🧪 兼容性测试

### 测试模块结构
```
compatibility-tests/
├── spring-boot-2-tests/    # Spring Boot 2.x 测试
└── spring-boot-3-tests/    # Spring Boot 3.x 测试
```

### 运行测试

**快速测试所有版本：**
```bash
./test-compatibility.sh
```

**单独测试 Spring Boot 2：**
```bash
mvn clean install -P spring-boot-2
cd compatibility-tests/spring-boot-2-tests
mvn test
```

**单独测试 Spring Boot 3：**
```bash
mvn clean install -P spring-boot-3
cd compatibility-tests/spring-boot-3-tests
mvn test
```

### 测试覆盖

- ✅ 应用上下文加载
- ✅ Spring Boot 版本验证
- ✅ Bean 注册和依赖注入
- ✅ Web 环境配置
- ✅ Jakarta EE 命名空间（Spring Boot 3）

## 📝 迁移指南

### 对于使用者

1. **确定你的 Spring Boot 版本**
   ```bash
   mvn dependency:tree | grep spring-boot
   ```

2. **选择对应的 logic-ide 版本**
   - Spring Boot 2.x → `logic-ide`
   - Spring Boot 3.x → `logic-ide-spring-boot-3`

3. **更新依赖**
   ```xml
   <!-- Spring Boot 2 -->
   <dependency>
       <groupId>com.aims.logic</groupId>
       <artifactId>logic-ide</artifactId>
       <version>${logic-ide.version}</version>
   </dependency>
   
   <!-- Spring Boot 3 -->
   <dependency>
       <groupId>com.aims.logic</groupId>
       <artifactId>logic-ide-spring-boot-3</artifactId>
       <version>${logic-ide.version}</version>
   </dependency>
   ```

### 对于开发者

1. **本地开发默认使用 Spring Boot 2**
   ```bash
   mvn clean install
   ```

2. **测试 Spring Boot 3 兼容性**
   ```bash
   mvn clean install -P spring-boot-3
   ```

3. **发布前运行完整测试**
   ```bash
   ./test-compatibility.sh
   ```

## 🔍 依赖冲突排查

### 查看依赖树
```bash
mvn dependency:tree -P spring-boot-2
mvn dependency:tree -P spring-boot-3
```

### 查看有效 POM
```bash
mvn help:effective-pom -P spring-boot-2
mvn help:effective-pom -P spring-boot-3
```

### 分析依赖冲突
```bash
mvn dependency:analyze
```

## 🚀 CI/CD 集成

### GitHub Actions 示例
```yaml
name: Compatibility Tests

on: [push, pull_request]

jobs:
  test-spring-boot-2:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Test Spring Boot 2
        run: |
          mvn clean install -P spring-boot-2
          cd compatibility-tests/spring-boot-2-tests
          mvn test

  test-spring-boot-3:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Test Spring Boot 3
        run: |
          mvn clean install -P spring-boot-3
          cd compatibility-tests/spring-boot-3-tests
          mvn test
```

## 📚 参考资料

- [Spring Boot 2 to 3 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Redisson Spring Boot Starter](https://github.com/redisson/redisson/tree/master/redisson-spring-boot-starter)
- [Maven Profiles](https://maven.apache.org/guides/introduction/introduction-to-profiles.html)
