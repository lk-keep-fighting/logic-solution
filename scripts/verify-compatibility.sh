#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MVNW_BIN="${PROJECT_ROOT}/mvnw"

if [[ ! -x "${MVNW_BIN}" ]]; then
  echo "Maven Wrapper not found or not executable at: ${MVNW_BIN}" >&2
  exit 1
fi

if [[ -z "${JAVA_HOME:-}" ]]; then
  echo "JAVA_HOME is not set. Please export JAVA_HOME to a JDK 17 installation before running this script." >&2
  exit 1
fi

echo "========================================"
echo "Logic-IDE 兼容性验证脚本"
echo "========================================"
echo "项目根目录: ${PROJECT_ROOT}"
echo "Java版本: $(java -version 2>&1 | head -n 1)"
echo "Maven版本: $("${MVNW_BIN}" --version | head -n 1)"
echo ""

profiles=("spring-boot-2" "spring-boot-3")

# 检查版本变更
echo "检查版本变更..."
CURRENT_VERSION=$("${MVNW_BIN}" -q -N help:evaluate -Dexpression=project.version -DforceStdout)
echo "当前版本: ${CURRENT_VERSION}"

# 运行兼容性测试
FAILED_PROFILES=()
for profile in "${profiles[@]}"; do
  echo ""
  echo "========================================"
  echo "运行 Maven verify - Profile: ${profile}"
  echo "========================================"

  if "${MVNW_BIN}" -pl logic-ide -am clean verify -P"${profile}" "$@"; then
    echo "✅ Profile ${profile} 验证成功"
  else
    echo "❌ Profile ${profile} 验证失败"
    FAILED_PROFILES+=("${profile}")
  fi
done

echo ""
echo "========================================"
echo "验证结果汇总"
echo "========================================"

if [ ${#FAILED_PROFILES[@]} -eq 0 ]; then
  echo "🎉 所有 profile 验证通过！版本 ${CURRENT_VERSION} 兼容 Spring Boot 2 & 3"
  exit 0
else
  echo "💥 以下 profile 验证失败: ${FAILED_PROFILES[*]}"
  echo "请检查错误信息并修复后重试"
  exit 1
fi
