#!/bin/bash

# 清除 HBuilder 缓存并准备修复脚本
echo "正在清除 HBuilder 缓存..."

# 清除打包缓存
rm -rf "/Users/fei/Library/Application Support/HBuilder X/__UNI__8D34897/packge_cache" 2>/dev/null
echo "✓ 打包缓存已清除"

# 清除 Apktool 框架缓存（可选，可能需要重新下载）
# rm -rf "/Users/fei/Library/apktool/framework/1.apk" 2>/dev/null
# echo "✓ Apktool 框架缓存已清除"

echo ""
echo "请在 HBuilder X 中："
echo "1. 关闭当前项目"
echo "2. 重新打开项目"
echo "3. 开始云打包"
echo ""
echo "如果问题仍然存在，请尝试："
echo "- 升级 HBuilder X 到最新版本"
echo "- 或者联系 DCloud 技术支持"
