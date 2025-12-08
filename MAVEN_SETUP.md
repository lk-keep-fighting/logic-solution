# Maven 环境配置说明

## 📋 概述

本项目支持两种 Maven 使用方式：
1. **系统 Maven** - 适用于 CI/CD 环境和已安装 Maven 的开发环境
2. **Maven Wrapper** - 适用于本地开发，无需安装 Maven

## 🔧 智能检测机制

项目中的脚本（如 `test-compatibility.sh`）会自动检测并选择合适的 Maven 命令：

```bash
# 检测逻辑
if command -v mvn &> /dev/null; then
    # 优先使用系统 Maven（CI/CD 环境）
    MVN_CMD="mvn"
elif [ -f "./mvnw" ]; then
    # 使用 Maven Wrapper（本地开发）
    MVN_CMD="./mvnw"
else
    # 报错退出
    echo "错误: 未找到 Maven"
    exit 1
fi
```

## 🚀 使用方式

### 方式 1: 使用 Maven Wrapper（推荐本地开发）

**无需安装 Maven**，直接使用项目自带的 Maven Wrapper：

```bash
# 查看版本
./mvnw --version

# 构建项目
./mvnw clean install

# 运行测试
./mvnw test

# 使用 Profile
./mvnw clean install -P spring-boot-2
./mvnw clean install -P spring-boot-3
```

**优势：**
- ✅ 无需安装 Maven
- ✅ 确保团队使用相同的 Maven 版本
- ✅ 开箱即用

### 方式 2: 安装系统 Maven（推荐 CI/CD）

#### macOS 安装

**使用 Homebrew（推荐）：**
```bash
# 安装 Homebrew（如果未安装）
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 安装 Maven
brew install maven

# 验证安装
mvn --version
```

**手动安装：**
```bash
# 1. 下载 Maven
curl -O https://dlcdn.apache.org/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.tar.gz

# 2. 解压
tar -xzf apache-maven-3.9.5-bin.tar.gz
sudo mv apache-maven-3.9.5 /opt/maven

# 3. 配置环境变量
echo 'export M2_HOME=/opt/maven' >> ~/.zshrc
echo 'export PATH=$M2_HOME/bin:$PATH' >> ~/.zshrc
source ~/.zshrc

# 4. 验证
mvn --version
```

#### Linux 安装

**Ubuntu/Debian：**
```bash
sudo apt update
sudo apt install maven
mvn --version
```

**CentOS/RHEL：**
```bash
sudo yum install maven
mvn --version
```

#### Windows 安装

**使用 Chocolatey：**
```powershell
choco install maven
mvn --version
```

**手动安装：**
1. 下载：https://maven.apache.org/download.cgi
2. 解压到 `C:\Program Files\Apache\maven`
3. 添加到 PATH：`C:\Program Files\Apache\maven\bin`
4. 验证：`mvn --version`

## 🧪 运行兼容性测试

### 使用自动化脚本（推荐）

```bash
# 脚本会自动检测并使用合适的 Maven 命令
./test-compatibility.sh
```

### 手动运行

**使用 Maven Wrapper：**
```bash
# Spring Boot 2
./mvnw clean install -P spring-boot-2
cd compatibility-tests/spring-boot-2-tests
../../mvnw test
cd ../..

# Spring Boot 3
./mvnw clean install -P spring-boot-3
cd compatibility-tests/spring-boot-3-tests
../../mvnw test
cd ../..
```

**使用系统 Maven：**
```bash
# Spring Boot 2
mvn clean install -P spring-boot-2
cd compatibility-tests/spring-boot-2-tests
mvn test
cd ../..

# Spring Boot 3
mvn clean install -P spring-boot-3
cd compatibility-tests/spring-boot-3-tests
mvn test
cd ../..
```

## 🔄 CI/CD 配置

### GitHub Actions

项目已配置使用 Maven Wrapper，确保 CI/CD 环境一致性：

```yaml
- name: Build with Spring Boot 2
  run: ./mvnw clean install -P spring-boot-2 -DskipTests

- name: Run tests
  run: |
    cd compatibility-tests/spring-boot-2-tests
    ../../mvnw test
```

**优势：**
- ✅ 无需在 CI 环境中安装 Maven
- ✅ 版本一致性
- ✅ 构建可重现

### Jenkins

```groovy
pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh './mvnw clean install -P spring-boot-2'
            }
        }
        stage('Test') {
            steps {
                sh './test-compatibility.sh'
            }
        }
    }
}
```

### GitLab CI

```yaml
build:
  script:
    - ./mvnw clean install -P spring-boot-2

test:
  script:
    - ./test-compatibility.sh
```

## 📊 命令对比

| 操作 | Maven Wrapper | 系统 Maven |
|------|---------------|------------|
| 查看版本 | `./mvnw --version` | `mvn --version` |
| 清理 | `./mvnw clean` | `mvn clean` |
| 编译 | `./mvnw compile` | `mvn compile` |
| 测试 | `./mvnw test` | `mvn test` |
| 打包 | `./mvnw package` | `mvn package` |
| 安装 | `./mvnw install` | `mvn install` |
| 使用 Profile | `./mvnw install -P spring-boot-2` | `mvn install -P spring-boot-2` |

## ❓ 常见问题

### Q1: 为什么推荐使用 Maven Wrapper？

**A:** Maven Wrapper 的优势：
- 无需手动安装 Maven
- 确保团队使用相同版本
- 简化新成员上手流程
- CI/CD 环境更一致

### Q2: Maven Wrapper 和系统 Maven 可以共存吗？

**A:** 可以！脚本会自动选择：
- 如果系统有 Maven，优先使用（CI/CD 环境）
- 如果没有，使用 Maven Wrapper（本地开发）

### Q3: 如何更新 Maven Wrapper 版本？

**A:** 
```bash
# 使用系统 Maven 更新 Wrapper
mvn wrapper:wrapper -Dmaven=3.9.5

# 或者手动下载新版本的 mvnw 和 mvnw.cmd
```

### Q4: Maven Wrapper 下载慢怎么办？

**A:** 配置国内镜像：

编辑 `.mvn/wrapper/maven-wrapper.properties`：
```properties
distributionUrl=https://mirrors.aliyun.com/apache/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.zip
```

### Q5: CI/CD 中应该使用哪种方式？

**A:** 推荐使用 Maven Wrapper：
- ✅ 版本一致性
- ✅ 无需预装 Maven
- ✅ 构建可重现

但如果 CI 环境已经安装了 Maven，直接使用也可以。

## 🎯 最佳实践

### 本地开发
```bash
# 使用 Maven Wrapper
./mvnw clean install
./test-compatibility.sh
```

### CI/CD 环境
```yaml
# 使用 Maven Wrapper（推荐）
script:
  - ./mvnw clean install
  - ./test-compatibility.sh
```

### 团队协作
1. 提交 `mvnw`、`mvnw.cmd` 和 `.mvn/` 到版本控制
2. 新成员无需安装 Maven，直接使用 `./mvnw`
3. 确保所有脚本支持两种方式

## 📚 相关资源

- [Maven 官方文档](https://maven.apache.org/)
- [Maven Wrapper 文档](https://maven.apache.org/wrapper/)
- [Homebrew 官网](https://brew.sh/)
- [项目兼容性测试指南](COMPATIBILITY_QUICK_START.md)
