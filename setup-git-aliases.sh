#!/bin/bash

# Logic IDE Git 别名设置脚本

echo "设置 Git 别名..."

# 快速推送别名
git config alias.push-github '!f() { 
    BRANCH=${1:-main}; 
    MSG=${2:-"Update from internal development"}; 
    echo "推送到 GitHub: $BRANCH"; 
    git push github $BRANCH; 
}; f'

git config alias.push-company '!f() { 
    BRANCH=${1:-master}; 
    echo "推送到公司仓库: $BRANCH"; 
    git push origin $BRANCH; 
}; f'

# 同步推送到两个远程仓库
git config alias.push-both '!f() { 
    BRANCH_GITHUB=${1:-main}; 
    BRANCH_COMPANY=${2:-master}; 
    echo "同时推送到两个仓库..."; 
    git push github $BRANCH_GITHUB && git push origin $BRANCH_COMPANY; 
}; f'

# 查看所有远程仓库状态
git config alias.remote-status '!git remote -v && echo "" && git branch -r'

echo "✅ Git 别名设置完成!"
echo ""
echo "可用的命令："
echo "  git push-github [分支名]     - 推送到 GitHub"
echo "  git push-company [分支名]    - 推送到公司仓库"  
echo "  git push-both [github分支] [公司分支] - 同时推送到两个仓库"
echo "  git remote-status            - 查看远程仓库状态"