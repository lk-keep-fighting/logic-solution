#!/bin/bash

# Logic-IDE 兼容性测试脚本
# 用于测试 Spring Boot 2 和 3 的兼容性

set -e

echo "=========================================="
echo "Logic-IDE 兼容性测试"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试结果
SB2_RESULT=0
SB3_RESULT=0

# 智能检测 Maven 命令
# 优先使用系统 Maven（CI/CD 环境），否则使用 Maven Wrapper（本地开发）
if command -v mvn &> /dev/null; then
    MVN_CMD="mvn"
    echo "✓ 使用系统 Maven: $(mvn --version | head -1)"
elif [ -f "./mvnw" ]; then
    MVN_CMD="./mvnw"
    echo "✓ 使用 Maven Wrapper: $(./mvnw --version | head -1)"
else
    echo "${RED}✗ 错误: 未找到 Maven 或 Maven Wrapper${NC}"
    echo "  请安装 Maven 或确保 mvnw 文件存在"
    exit 1
fi
echo ""

echo "${YELLOW}步骤 1: 清理之前的构建${NC}"
$MVN_CMD clean

echo ""
echo "${YELLOW}步骤 2: 构建 Spring Boot 2 版本${NC}"
echo "----------------------------------------"
if $MVN_CMD clean install -P spring-boot-2 -DskipTests; then
    echo "${GREEN}✓ Spring Boot 2 版本构建成功${NC}"
else
    echo "${RED}✗ Spring Boot 2 版本构建失败${NC}"
    exit 1
fi

echo ""
echo "${YELLOW}步骤 3: 运行 Spring Boot 2 兼容性测试${NC}"
echo "----------------------------------------"
cd compatibility-tests/spring-boot-2-tests
if $MVN_CMD test; then
    echo "${GREEN}✓ Spring Boot 2 兼容性测试通过${NC}"
else
    echo "${RED}✗ Spring Boot 2 兼容性测试失败${NC}"
    SB2_RESULT=1
fi
cd ../..

echo ""
echo "${YELLOW}步骤 4: 构建 Spring Boot 3 版本${NC}"
echo "----------------------------------------"
if $MVN_CMD clean install -P spring-boot-3 -DskipTests; then
    echo "${GREEN}✓ Spring Boot 3 版本构建成功${NC}"
else
    echo "${RED}✗ Spring Boot 3 版本构建失败${NC}"
    exit 1
fi

echo ""
echo "${YELLOW}步骤 5: 运行 Spring Boot 3 兼容性测试${NC}"
echo "----------------------------------------"
cd compatibility-tests/spring-boot-3-tests
if $MVN_CMD test; then
    echo "${GREEN}✓ Spring Boot 3 兼容性测试通过${NC}"
else
    echo "${RED}✗ Spring Boot 3 兼容性测试失败${NC}"
    SB3_RESULT=1
fi
cd ../..

echo ""
echo "=========================================="
echo "测试结果汇总"
echo "=========================================="
if [ $SB2_RESULT -eq 0 ]; then
    echo "${GREEN}✓ Spring Boot 2 兼容性: 通过${NC}"
else
    echo "${RED}✗ Spring Boot 2 兼容性: 失败${NC}"
fi

if [ $SB3_RESULT -eq 0 ]; then
    echo "${GREEN}✓ Spring Boot 3 兼容性: 通过${NC}"
else
    echo "${RED}✗ Spring Boot 3 兼容性: 失败${NC}"
fi

echo ""
if [ $SB2_RESULT -eq 0 ] && [ $SB3_RESULT -eq 0 ]; then
    echo "${GREEN}=========================================="
    echo "所有兼容性测试通过！"
    echo "==========================================${NC}"
    exit 0
else
    echo "${RED}=========================================="
    echo "部分兼容性测试失败"
    echo "==========================================${NC}"
    exit 1
fi
