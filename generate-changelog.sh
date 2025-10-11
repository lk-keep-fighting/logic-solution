#!/bin/bash

# generate-changelog.sh - 自动生成 CHANGELOG.md
# 基于 git commit 历史生成规范化的 changelog

set -e

CHANGELOG_FILE="CHANGELOG.md"
PROJECT_NAME="Logic IDE"
REPO_URL="https://github.com/liusir2606/logic-ide"

# 获取最新的 tag
LATEST_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "")
PREVIOUS_TAG=""

if [ -n "$LATEST_TAG" ]; then
    # 获取前一个 tag
    PREVIOUS_TAG=$(git describe --tags --abbrev=0 "$LATEST_TAG^" 2>/dev/null || echo "")
fi

# 生成 changelog 头部
generate_header() {
    cat > $CHANGELOG_FILE << EOF
# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

EOF
}

# 解析 commit 类型
parse_commit_type() {
    local commit_msg="$1"
    case "$commit_msg" in
        feat:*|feat\(*\):*) echo "### ✨ 新功能" ;;
        fix:*|fix\(*\):*) echo "### 🐛 问题修复" ;;
        docs:*|docs\(*\):*) echo "### 📚 文档" ;;
        style:*|style\(*\):*) echo "### 💄 代码样式" ;;
        refactor:*|refactor\(*\):*) echo "### ♻️ 代码重构" ;;
        perf:*|perf\(*\):*) echo "### ⚡ 性能优化" ;;
        test:*|test\(*\):*) echo "### ✅ 测试" ;;
        chore:*|chore\(*\):*) echo "### 🔧 构建/工具" ;;
        *) echo "### 🔄 其他变更" ;;
    esac
}

# 格式化 commit 消息
format_commit() {
    local hash="$1"
    local msg="$2"
    local short_hash=$(echo "$hash" | cut -c1-7)
    
    # 移除 conventional commit 前缀
    local clean_msg=$(echo "$msg" | sed -E 's/^(feat|fix|docs|style|refactor|perf|test|chore)(\\([^)]*\\))?: //')
    
    echo "- $clean_msg ([${short_hash}]($REPO_URL/commit/$hash))"
}

# 生成指定范围的 changelog
generate_range() {
    local from_tag="$1"
    local to_tag="$2"
    local version_name="$3"
    
    echo "## [$version_name] - $(date +%Y-%m-%d)"
    echo ""
    
    # 获取该范围内的 commit
    local range_cmd
    if [ -n "$from_tag" ]; then
        range_cmd="$from_tag..$to_tag"
    else
        range_cmd="$to_tag"
    fi
    
    # 按类型分类 commits
    declare -A commit_types
    while IFS=$'\t' read -r hash msg; do
        local type_header=$(parse_commit_type "$msg")
        if [ -z "${commit_types[$type_header]}" ]; then
            commit_types[$type_header]=""
        fi
        commit_types[$type_header]+="$(format_commit "$hash" "$msg")"$'\n'
    done < <(git log --pretty=format:'%H%x09%s' --reverse $range_cmd 2>/dev/null || true)
    
    # 输出分类的 commits
    for type in "### ✨ 新功能" "### 🐛 问题修复" "### 📚 文档" "### ♻️ 代码重构" "### ⚡ 性能优化" "### ✅ 测试" "### 🔧 构建/工具" "### 🔄 其他变更"; do
        if [ -n "${commit_types[$type]}" ]; then
            echo "$type"
            echo ""
            echo -n "${commit_types[$type]}"
            echo ""
        fi
    done
}

# 主函数
main() {
    echo "🚀 正在生成 $PROJECT_NAME 的 CHANGELOG..."
    
    # 备份原有的 changelog
    if [ -f "$CHANGELOG_FILE" ]; then
        cp "$CHANGELOG_FILE" "${CHANGELOG_FILE}.bak"
        echo "📦 已备份原有 changelog 到 ${CHANGELOG_FILE}.bak"
    fi
    
    # 生成新的 changelog
    generate_header
    
    # 获取所有 tags，按版本排序
    local tags=($(git tag --sort=-version:refname))
    
    if [ ${#tags[@]} -eq 0 ]; then
        echo "⚠️  未找到任何 tag，生成未发布的更改..."
        generate_range "" "HEAD" "Unreleased"
    else
        # 首先生成未发布的更改
        local unreleased_commits=$(git log "${tags[0]}..HEAD" --oneline 2>/dev/null | wc -l || echo "0")
        if [ "$unreleased_commits" -gt 0 ]; then
            generate_range "${tags[0]}" "HEAD" "Unreleased"
        fi
        
        # 为每个 tag 生成 changelog
        for i in "${!tags[@]}"; do
            local current_tag="${tags[$i]}"
            local previous_tag=""
            if [ $((i+1)) -lt ${#tags[@]} ]; then
                previous_tag="${tags[$((i+1))]}"
            fi
            
            generate_range "$previous_tag" "$current_tag" "${current_tag#v}"
        done
    fi >> "$CHANGELOG_FILE"
    
    echo "✅ CHANGELOG.md 生成完成！"
    echo "📝 生成的文件: $CHANGELOG_FILE"
    
    if command -v wc >/dev/null 2>&1; then
        local line_count=$(wc -l < "$CHANGELOG_FILE")
        echo "📊 共 $line_count 行"
    fi
}

# 检查是否在 git 仓库中
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    echo "❌ 错误: 当前目录不是 git 仓库"
    exit 1
fi

# 运行主函数
main "$@"