#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MVNW_BIN="${PROJECT_ROOT}/mvnw"

# 默认参数
SKIP_TESTS=false
SKIP_DEPLOY=false
DRY_RUN=false
PROFILES=("spring-boot-2" "spring-boot-3")

# 解析命令行参数
while [[ $# -gt 0 ]]; do
  case $1 in
    --skip-tests)
      SKIP_TESTS=true
      shift
      ;;
    --skip-deploy)
      SKIP_DEPLOY=true
      shift
      ;;
    --dry-run)
      DRY_RUN=true
      shift
      ;;
    --profiles)
      IFS=',' read -ra PROFILES <<< "$2"
      shift 2
      ;;
    -h|--help)
      echo "Logic-IDE 构建与发布脚本"
      echo ""
      echo "用法: $0 [选项]"
      echo ""
      echo "选项:"
      echo "  --skip-tests    跳过测试"
      echo "  --skip-deploy   跳过部署到仓库"
      echo "  --dry-run       模拟运行，不执行实际操作"
      echo "  --profiles      指定要构建的profiles (逗号分隔)"
      echo "  -h, --help      显示帮助信息"
      echo ""
      echo "示例:"
      echo "  $0                           # 构建所有profiles"
      echo "  $0 --profiles spring-boot-2  # 只构建Spring Boot 2"
      echo "  $0 --dry-run                 # 模拟运行"
      exit 0
      ;;
    *)
      echo "未知参数: $1"
      echo "使用 -h 或 --help 查看帮助"
      exit 1
      ;;
  esac
done

echo "========================================"
echo "Logic-IDE 构建与发布脚本"
echo "========================================"
echo "项目根目录: ${PROJECT_ROOT}"
echo "Java版本: $(java -version 2>&1 | head -n 1)"
echo "Maven版本: $("${MVNW_BIN}" --version | head -n 1)"
echo "构建Profiles: ${PROFILES[*]}"
echo "跳过测试: ${SKIP_TESTS}"
echo "跳过部署: ${SKIP_DEPLOY}"
echo "模拟运行: ${DRY_RUN}"
echo ""

# 获取当前版本
CURRENT_VERSION=$("${MVNW_BIN}" -q -N help:evaluate -Dexpression=project.version -DforceStdout)
echo "当前版本: ${CURRENT_VERSION}"

# 检查版本是否为SNAPSHOT
if [[ "$CURRENT_VERSION" == *"-SNAPSHOT" ]]; then
  echo "⚠️  当前为SNAPSHOT版本，将部署到快照仓库"
  REPO_TYPE="snapshots"
else
  echo "✅ 当前为正式版本，将部署到发布仓库"
  REPO_TYPE="releases"
fi

echo ""

# 构建函数
build_profile() {
  local profile="$1"
  echo ""
  echo "========================================"
  echo "构建 Profile: ${profile}"
  echo "========================================"

  local maven_goals=("clean")

  if [[ "$SKIP_TESTS" == false ]]; then
    maven_goals+=("verify")
  else
    maven_goals+=("install")
  fi

  local maven_args=(
    "-pl" "logic-ide"
    "-am"
    "-P${profile}"
  )

  if [[ "$DRY_RUN" == false ]]; then
    echo "执行命令: ${MVNW_BIN} ${maven_goals[*]} ${maven_args[*]}"
    if "${MVNW_BIN}" "${maven_goals[@]}" "${maven_args[@]}"; then
      echo "✅ Profile ${profile} 构建成功"
      return 0
    else
      echo "❌ Profile ${profile} 构建失败"
      return 1
    fi
  else
    echo "模拟执行: ${MVNW_BIN} ${maven_goals[*]} ${maven_args[*]}"
    echo "✅ Profile ${profile} 模拟构建成功"
    return 0
  fi
}

# 部署函数
deploy_profile() {
  local profile="$1"
  echo ""
  echo "========================================"
  echo "部署 Profile: ${profile}"
  echo "========================================"

  if [[ "$SKIP_DEPLOY" == true ]]; then
    echo "⏭️  跳过部署 (skip-deploy)"
    return 0
  fi

  if [[ "$DRY_RUN" == false ]]; then
    echo "执行部署命令..."
    if "${MVNW_BIN}" -pl logic-ide -am -P"${profile}" deploy -DskipTests; then
      echo "✅ Profile ${profile} 部署成功"
      return 0
    else
      echo "❌ Profile ${profile} 部署失败"
      return 1
    fi
  else
    echo "模拟部署 Profile ${profile}"
    echo "✅ Profile ${profile} 模拟部署成功"
    return 0
  fi
}

# 执行构建
FAILED_PROFILES=()
for profile in "${PROFILES[@]}"; do
  if build_profile "$profile"; then
    echo "📦 Profile ${profile} 构建完成"
  else
    FAILED_PROFILES+=("${profile}")
  fi
done

# 检查构建结果
if [ ${#FAILED_PROFILES[@]} -ne 0 ]; then
  echo ""
  echo "========================================"
  echo "构建失败"
  echo "========================================"
  echo "以下 profile 构建失败: ${FAILED_PROFILES[*]}"
  exit 1
fi

# 执行部署
FAILED_DEPLOYS=()
for profile in "${PROFILES[@]}"; do
  if ! deploy_profile "$profile"; then
    FAILED_DEPLOYS+=("${profile}")
  fi
done

# 最终结果
echo ""
echo "========================================"
echo "构建与发布结果汇总"
echo "========================================"

if [ ${#FAILED_DEPLOYS[@]} -eq 0 ]; then
  echo "🎉 所有操作完成！"
  echo "版本: ${CURRENT_VERSION}"
  echo "构建的Profiles: ${PROFILES[*]}"

  if [[ "$SKIP_DEPLOY" == false && "$DRY_RUN" == false ]]; then
    echo "✅ 已部署到 ${REPO_TYPE} 仓库"

    # 提示创建Git tag
    if [[ "$REPO_TYPE" == "releases" ]]; then
      echo ""
      echo "💡 建议创建Git tag:"
      echo "   git tag v${CURRENT_VERSION}"
      echo "   git push origin v${CURRENT_VERSION}"
    fi
  fi
else
  echo "💥 以下 profile 部署失败: ${FAILED_DEPLOYS[*]}"
  exit 1
fi