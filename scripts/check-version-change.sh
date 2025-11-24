#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MVNW_BIN="${PROJECT_ROOT}/mvnw"

if [[ ! -x "${MVNW_BIN}" ]]; then
  echo "Maven Wrapper not found or not executable at: ${MVNW_BIN}" >&2
  exit 1
fi

echo "========================================"
echo "Logic-IDE 版本变更检测脚本"
echo "========================================"

# 检查是否在 Git 仓库中
if ! git rev-parse --git-dir > /dev/null 2>&1; then
  echo "❌ 当前目录不是 Git 仓库"
  exit 1
fi

# 获取当前和上一个提交的 hash
CURRENT_COMMIT=$(git rev-parse HEAD)
PREV_COMMIT=$(git rev-parse HEAD^ 2>/dev/null || echo "")

if [[ -z "${PREV_COMMIT}" ]]; then
  echo "⚠️  没有上一个提交，这是初始提交"
  PREV_COMMIT="4b825dc642cb6eb9a060e54bf8d69288fbee4904" # Empty tree hash
fi

echo "当前提交: ${CURRENT_COMMIT}"
echo "上一个提交: ${PREV_COMMIT}"
echo ""

# 检查版本定义文件是否变更
VERSION_FILES=("pom.xml" "logic-ide/pom.xml" "logic-sdk/pom.xml")
CHANGED_VERSION_FILES=()

for file in "${VERSION_FILES[@]}"; do
  if git diff --name-only "${PREV_COMMIT}" "${CURRENT_COMMIT}" | grep -q "^${file}$"; then
    CHANGED_VERSION_FILES+=("${file}")
  fi
done

if [ ${#CHANGED_VERSION_FILES[@]} -eq 0 ]; then
  echo "📋 版本定义文件未发生变更"
  echo "检测的文件: ${VERSION_FILES[*]}"
  exit 0
fi

echo "📝 检测到版本定义文件变更: ${CHANGED_VERSION_FILES[*]}"
echo ""

# 提取版本号并比较
echo "提取版本号..."
PREV_VERSION=$(git show "${PREV_COMMIT}:pom.xml" 2>/dev/null | "${MVNW_BIN}" -q -N help:evaluate -Dexpression=project.version -DforceStdout 2>/dev/null || echo "unknown")
CURRENT_VERSION=$("${MVNW_BIN}" -q -N help:evaluate -Dexpression=project.version -DforceStdout 2>/dev/null || echo "unknown")

echo "上一个版本: ${PREV_VERSION}"
echo "当前版本:   ${CURRENT_VERSION}"
echo ""

# 检查是否为语义化版本提升
function is_version_bumped() {
  local prev="$1"
  local curr="$2"

  # 处理未知版本
  if [[ "$prev" == "unknown" || "$curr" == "unknown" ]]; then
    return 1
  fi

  # 提取版本号（去掉 -SNAPSHOT 等后缀）
  prev_clean=$(echo "$prev" | sed 's/-SNAPSHOT$//')
  curr_clean=$(echo "$curr" | sed 's/-SNAPSHOT$//')

  # 简单的版本号比较
  if [[ "$prev_clean" != "$curr_clean" ]]; then
    return 0
  fi

  return 1
}

if is_version_bumped "$PREV_VERSION" "$CURRENT_VERSION"; then
  echo "🎉 检测到版本提升: ${PREV_VERSION} -> ${CURRENT_VERSION}"
  echo "version_changed=true"
  exit 0
else
  echo "📋 版本号未发生语义化提升"

  # 检查提交信息是否包含发布关键词
  COMMIT_MSG=$(git log --format=%B -n 1 "${CURRENT_COMMIT}")
  if echo "$COMMIT_MSG" | grep -qiE "release|发布|版本"; then
    echo "📝 提交信息包含发布关键词，触发发布流程"
    echo "version_changed=true"
    exit 0
  fi

  echo "version_changed=false"
  exit 0
fi