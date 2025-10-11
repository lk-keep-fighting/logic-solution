#!/bin/bash

# gen-changelog-simple.sh - 简化版 changelog 生成器
# 基于 git log 生成 CHANGELOG.md

set -e

CHANGELOG_FILE="CHANGELOG.md"
REPO_URL="https://github.com/liusir2606/logic-ide"

echo "🚀 生成 Logic IDE changelog..."

# 备份现有文件
if [ -f "$CHANGELOG_FILE" ]; then
    cp "$CHANGELOG_FILE" "${CHANGELOG_FILE}.bak"
    echo "📦 备份原文件到 ${CHANGELOG_FILE}.bak"
fi

# 生成新的 changelog
cat > $CHANGELOG_FILE << 'EOF'
# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

EOF

# 获取所有 tags
TAGS=$(git tag --sort=-version:refname 2>/dev/null || echo "")

if [ -z "$TAGS" ]; then
    echo "## [Unreleased] - $(date +%Y-%m-%d)" >> $CHANGELOG_FILE
    echo "" >> $CHANGELOG_FILE
    git log --pretty=format:"- %s ([%h]($REPO_URL/commit/%H))" --reverse >> $CHANGELOG_FILE
    echo "" >> $CHANGELOG_FILE
else
    # 生成未发布的变更
    LATEST_TAG=$(echo "$TAGS" | head -n1)
    UNRELEASED_COUNT=$(git rev-list ${LATEST_TAG}..HEAD --count 2>/dev/null || echo 0)
    
    if [ "$UNRELEASED_COUNT" -gt 0 ]; then
        echo "## [Unreleased] - $(date +%Y-%m-%d)" >> $CHANGELOG_FILE
        echo "" >> $CHANGELOG_FILE
        git log --pretty=format:"- %s ([%h]($REPO_URL/commit/%H))" --reverse ${LATEST_TAG}..HEAD >> $CHANGELOG_FILE
        echo "" >> $CHANGELOG_FILE
        echo "" >> $CHANGELOG_FILE
    fi
    
    # 为每个 tag 生成 changelog
    PREV_TAG=""
    for TAG in $TAGS; do
        echo "## [${TAG#v}] - $(git log -1 --format=%ad --date=short $TAG)" >> $CHANGELOG_FILE
        echo "" >> $CHANGELOG_FILE
        
        if [ -n "$PREV_TAG" ]; then
            git log --pretty=format:"- %s ([%h]($REPO_URL/commit/%H))" --reverse ${PREV_TAG}..${TAG} >> $CHANGELOG_FILE
        else
            git log --pretty=format:"- %s ([%h]($REPO_URL/commit/%H))" --reverse ${TAG} >> $CHANGELOG_FILE
        fi
        
        echo "" >> $CHANGELOG_FILE
        echo "" >> $CHANGELOG_FILE
        PREV_TAG=$TAG
    done
fi

echo "✅ CHANGELOG.md 生成完成！"
echo "📄 $(wc -l < "$CHANGELOG_FILE") 行内容"