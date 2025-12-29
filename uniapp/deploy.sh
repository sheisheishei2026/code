#!/bin/bash
set -e

# 确保脚本在 uni-travel 目录下运行
cd "$(dirname "$0")"

# ==========================================
# GitHub Pages 部署配置
# 请务必修改以下信息！
# ==========================================

# 1. 你的 GitHub 用户名
USERNAME="PeiZhiFei"

# 2. 你的 GitHub 仓库名
REPO="uni-travel"

# 3. 自定义域名 (例如: www.example.com)
# 如果不需要自定义域名，请将此行保持注释或留空
# CUSTOM_DOMAIN="your-domain.com"

# ==========================================

# 检查配置是否已填写
if [[ "$USERNAME" == "<USERNAME>" ]] || [[ "$REPO" == "<REPO>" ]]; then
  echo "错误: 请先打开 deploy.sh 文件，填入你的 USERNAME 和 REPO。"
  exit 1
fi

echo "开始构建 UniApp H5 项目..."
npm run build:h5

echo "进入构建产物目录..."
cd dist/build/h5

echo "初始化 git 仓库..."
git init

# 创建 .nojekyll 文件，防止 GitHub Pages 忽略下划线开头的文件
touch .nojekyll

# 如果设置了自定义域名，生成 CNAME 文件
if [ -n "$CUSTOM_DOMAIN" ]; then
  echo "生成 CNAME 文件: $CUSTOM_DOMAIN"
  echo "$CUSTOM_DOMAIN" > CNAME
fi

echo "添加文件到暂存区..."
git add -A
git commit -m 'Deploy to GitHub Pages'

echo "推送到 gh-pages 分支..."
# 使用 SSH 格式推送
git push -f git@github.com:$USERNAME/$REPO.git main:gh-pages

echo "部署完成!"
