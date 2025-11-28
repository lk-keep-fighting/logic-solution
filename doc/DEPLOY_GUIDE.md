# 发布到 Nexus 私库指南

## 前置条件

1. 确保你有 Nexus 私库的访问权限
2. 获取 Nexus 用户名和密码

## 配置方式

### 方式一：使用环境变量（推荐用于 CI/CD）

```bash
export NEXUS_USERNAME=your_username
export NEXUS_PASSWORD=your_password
```

### 方式二：配置本地 Maven settings.xml（推荐用于本地开发）

编辑 `~/.m2/settings.xml`，添加以下配置：

```xml
<settings>
    <servers>
        <server>
            <id>nexus-releases</id>
            <username>your_username</username>
            <password>your_password</password>
        </server>
        <server>
            <id>nexus-snapshots</id>
            <username>your_username</username>
            <password>your_password</password>
        </server>
    </servers>
    
    <mirrors>
        <mirror>
            <id>nexus</id>
            <mirrorOf>*</mirrorOf>
            <url>https://nexus.aimstek.cn/repository/maven-public</url>
        </mirror>
    </mirrors>
</settings>
```

**注意**：如果 `~/.m2/settings.xml` 文件不存在，需要先创建：
```bash
mkdir -p ~/.m2
touch ~/.m2/settings.xml
```

## 发布步骤

### 手动发布

#### 1. 发布所有版本（推荐）

```bash
# 同时构建和发布 Spring Boot 2 & 3 版本
./scripts/build-and-release.sh
```

#### 2. 发布单个版本

```bash
# 只发布 Spring Boot 2
./scripts/build-and-release.sh --profiles spring-boot-2

# 只发布 Spring Boot 3
./scripts/build-and-release.sh --profiles spring-boot-3

# 或直接使用 Maven 命令
mvn clean deploy -Pspring-boot-2 -DskipTests
```

#### 3. CI/CD 环境发布（使用环境变量认证）

```bash
# 设置环境变量
export NEXUS_USERNAME=your_username
export NEXUS_PASSWORD=your_password

# 脚本会自动检测环境变量并使用 .github/maven-settings.xml
./scripts/build-and-release.sh

# 或手动指定 settings 文件
./scripts/build-and-release.sh --settings .github/maven-settings.xml
```

#### 3. 仅发布特定模块

```bash
# 仅发布 logic-runtime
mvn -pl logic-runtime -am clean deploy -Pspring-boot-2

# 仅发布 logic-sdk
mvn -pl logic-sdk -am clean deploy -Pspring-boot-2

# 仅发布 logic-ide
mvn -pl logic-ide -am clean deploy -Pspring-boot-2
```

### 使用 CI/CD 自动发布

参考 `.github/workflows/` 目录下的 GitHub Actions 配置（待创建）。

#### 脚本高级选项

```bash
# 查看所有选项
./scripts/build-and-release.sh --help

# 常用组合
./scripts/build-and-release.sh --skip-tests              # 跳过测试快速发布
./scripts/build-and-release.sh --skip-deploy            # 只构建不发布
./scripts/build-and-release.sh --dry-run                # 模拟运行
./scripts/build-and-release.sh --profiles spring-boot-2 # 只构建单个版本
```

## 版本管理

当前项目使用 `${revision}` 变量管理版本：

- 在根 `pom.xml` 中修改 `<revision>` 属性
- 当前版本：`0.10-SNAPSHOT`
- SNAPSHOT 版本会发布到 `nexus-snapshots` 仓库
- 正式版本会发布到 `nexus-releases` 仓库

### 发布正式版本

1. 修改根 `pom.xml` 中的版本号，去掉 `-SNAPSHOT` 后缀：
   ```xml
   <revision>0.10.0</revision>
   ```

2. 提交并打标签：
   ```bash
   git add pom.xml
   git commit -m "release: 0.10.0"
   git tag v0.10.0
   git push origin main --tags
   ```

3. 发布到私库：
   ```bash
   ./scripts/build-and-release.sh
   ```

4. 升级到下一个开发版本：
   ```xml
   <revision>0.11-SNAPSHOT</revision>
   ```

## 发布的模块

以下模块会被发布到私库：

- ✅ `logic-runtime` - 运行时核心
- ✅ `logic-sdk` - SDK 核心
- ✅ `logic-ide` - IDE 集成
- ❌ `test-suite` - 测试模块（已配置跳过发布）

## 验证发布

发布成功后，可以在 Nexus 私库中查看：

- Releases: https://nexus.aimstek.cn/repository/maven-releases/
- Snapshots: https://nexus.aimstek.cn/repository/maven-snapshots/

或在其他项目中引用：

```xml
<dependency>
    <groupId>com.aims.logic</groupId>
    <artifactId>logic-ide</artifactId>
    <version>0.10-SNAPSHOT</version>
</dependency>
```

## 常见问题

### 1. 401 Unauthorized

检查 Nexus 用户名和密码是否正确配置。

### 2. 400 Bad Request - Repository does not allow updating assets

正式版本（非 SNAPSHOT）不能重复发布，需要升级版本号。

### 3. 编译失败

确保先运行 `mvn clean install` 编译所有模块。

### 4. 依赖解析失败

检查 `~/.m2/settings.xml` 中是否配置了正确的镜像仓库。
