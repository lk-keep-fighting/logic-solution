#!/bin/bash

# GitHub SSH 连接测试脚本

echo "🔐 测试 GitHub SSH 连接..."
echo ""

# 测试 SSH 连接
if ssh -T git@github.com 2>&1 | grep -q "successfully authenticated"; then
    echo "✅ SSH 连接成功！"
    echo ""
    echo "现在可以使用推送脚本："
    echo "  ./push-to-github.sh"
    echo ""
    
    # 显示当前 Remote 配置
    echo "📍 当前 Remote 配置："
    git remote -v
    
elif ssh -T git@github.com 2>&1 | grep -q "Permission denied"; then
    echo "❌ SSH 密钥未添加到 GitHub"
    echo ""
    echo "请按以下步骤添加 SSH 密钥："
    echo "1. 复制下面的公钥："
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    cat ~/.ssh/id_ed25519.pub
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "2. 打开 https://github.com/settings/keys"
    echo "3. 点击 'New SSH key'"
    echo "4. Title: MacBook Logic IDE"
    echo "5. Key: 粘贴上面的公钥"
    echo "6. 点击 'Add SSH key'"
    echo ""
    echo "添加完成后再次运行: ./test-github-ssh.sh"
    
else
    echo "🔄 正在测试连接..."
    ssh -T git@github.com
fi