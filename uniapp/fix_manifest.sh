#!/bin/bash

# 修复 AndroidManifest.xml 中的 enableOnBackInvokedCallback 属性
# 该属性会导致 Apktool 2.6.1 打包失败

MANIFEST_PATH="/Users/fei/Library/Application Support/HBuilder X/__UNI__8D34897/packge_cache/__NONE__/__UNI__8D34897_cm/AndroidManifest.xml"

# 等待文件生成（最多等待 60 秒）
for i in {1..60}; do
    if [ -f "$MANIFEST_PATH" ]; then
        echo "找到 AndroidManifest.xml，开始修复..."
        
        # 移除 enableOnBackInvokedCallback 属性
        if grep -q "enableOnBackInvokedCallback" "$MANIFEST_PATH"; then
            # 使用 sed 移除该属性（包括可能的引号值）
            sed -i '' 's/android:enableOnBackInvokedCallback="[^"]*"//g' "$MANIFEST_PATH"
            sed -i '' 's/android:enableOnBackInvokedCallback=\x27[^\x27]*\x27//g' "$MANIFEST_PATH"
            sed -i '' 's/android:enableOnBackInvokedCallback=[^ ]*//g' "$MANIFEST_PATH"
            
            echo "已移除 enableOnBackInvokedCallback 属性"
        else
            echo "未找到 enableOnBackInvokedCallback 属性"
        fi
        exit 0
    fi
    sleep 1
done

echo "未找到 AndroidManifest.xml 文件"
exit 1

