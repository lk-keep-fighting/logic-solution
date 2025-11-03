#!/bin/bash

# Logic IDE GitHub 推送脚本
# 用法: ./push-to-github.sh [分支名] [提交信息]

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 默认参数
BRANCH=${1:-"main"}
COMMIT_MSG=${2:-"Update from internal development"}

echo -e "${BLUE}=== Logic IDE GitHub 推送工具 ===${NC}"
echo -e "${YELLOW}目标分支: $BRANCH${NC}"
echo -e "${YELLOW}提交信息: $COMMIT_MSG${NC}"
echo ""

# 检查是否有未提交的更改
if ! git diff-index --quiet HEAD -- 2>/dev/null; then
    echo -e "${YELLOW}检测到未提交的更改，正在提交...${NC}"
    git add .
    git commit -m "$COMMIT_MSG"
fi

# 检查当前分支
CURRENT_BRANCH=$(git branch --show-current)
echo -e "${BLUE}当前分支: $CURRENT_BRANCH${NC}"

# 如果不在目标分支，切换或创建
if [ "$CURRENT_BRANCH" != "$BRANCH" ]; then
    echo -e "${YELLOW}切换到分支: $BRANCH${NC}"
    if git show-ref --verify --quiet refs/heads/$BRANCH; then
        git checkout $BRANCH
    else
        echo -e "${YELLOW}创建新分支: $BRANCH${NC}"
        git checkout -b $BRANCH
    fi
    
    # 如果从其他分支切换过来，合并最新更改
    if [ "$CURRENT_BRANCH" != "$BRANCH" ] && [ "$CURRENT_BRANCH" != "" ]; then
        echo -e "${YELLOW}合并来自 $CURRENT_BRANCH 的更改...${NC}"
        git merge $CURRENT_BRANCH --no-edit
    fi
fi

# 推送到 GitHub
echo -e "${BLUE}推送到 GitHub Remote...${NC}"
git push github $BRANCH

# 显示推送结果
echo ""
echo -e "${GREEN}✅ 成功推送到 GitHub!${NC}"
echo -e "${GREEN}📍 仓库地址: https://github.com/lk-keep-fighting/logic-solution.git${NC}"
echo -e "${GREEN}🌿 分支: $BRANCH${NC}"
echo ""

# 显示 Remote 状态
echo -e "${BLUE}=== 当前 Remote 配置 ===${NC}"
git remote -v

echo ""
echo -e "${BLUE}=== 使用说明 ===${NC}"
echo -e "推送到主分支:     ${GREEN}./push-to-github.sh${NC}"
echo -e "推送到指定分支:   ${GREEN}./push-to-github.sh dev${NC}"
echo -e "自定义提交信息:   ${GREEN}./push-to-github.sh main \"feat: add new feature\"${NC}"
echo -e "公司内部推送:     ${GREEN}git push origin master${NC}"