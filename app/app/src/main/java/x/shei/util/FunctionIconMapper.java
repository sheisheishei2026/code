package x.shei.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 功能图标映射类
 * 为各个功能提供对应的图标资源链接
 */
public class FunctionIconMapper {
    
    private static final Map<String, String> ICON_MAP = new HashMap<>();
    
    static {
        // 为各个功能提供统一的拟物化风格图标（3D立体效果，带阴影和高光）
        ICON_MAP.put("X", "https://cdn-icons-png.flaticon.com/512/3048/3048427.png"); // 电影/视频 - 拟物化电影摄像机（3D立体）
        ICON_MAP.put("写真", "https://cdn-icons-png.flaticon.com/512/1829/1829519.png"); // 相册/图片 - 拟物化相册（3D立体）
        ICON_MAP.put("PDF", "https://cdn-icons-png.flaticon.com/512/337/337946.png"); // PDF文档 - 拟物化PDF文件（3D立体）
        ICON_MAP.put("扫码", "https://cdn-icons-png.flaticon.com/512/1041/1041916.png"); // 扫码 - 拟物化二维码扫描（3D立体）
        ICON_MAP.put("景点", "https://cdn-icons-png.flaticon.com/512/3231/3231922.png"); // 景点/位置 - 拟物化景点标记（3D立体）
        ICON_MAP.put("H5", "https://cdn-icons-png.flaticon.com/512/732/732212.png"); // 网页 - 拟物化浏览器（3D立体）
        ICON_MAP.put("应用", "https://cdn-icons-png.flaticon.com/512/906/906334.png"); // 应用 - 拟物化应用图标（3D立体）
        ICON_MAP.put("计算器", "https://cdn-icons-png.flaticon.com/512/1040/1040254.png"); // 计算器 - 拟物化计算器（3D立体）
        ICON_MAP.put("紧急", "https://cdn-icons-png.flaticon.com/512/686/686646.png"); // 紧急/警报 - 拟物化紧急警报（3D立体）
        ICON_MAP.put("音效", "https://cdn-icons-png.flaticon.com/512/727/727218.png"); // 音效/音频 - 拟物化音频图标（3D立体）
        ICON_MAP.put("防空", "https://cdn-icons-png.flaticon.com/512/1828/1828479.png"); // 防空/警报 - 拟物化防空警报（3D立体）
        ICON_MAP.put("应急", "https://cdn-icons-png.flaticon.com/512/2961/2961938.png"); // 应急/急救 - 拟物化急救箱（3D立体）
    }
    
    /**
     * 根据功能名称获取对应的图标资源链接
     */
    public static String getIconUrl(String functionName) {
        return ICON_MAP.getOrDefault(functionName, "https://cdn-icons-png.flaticon.com/512/126/126472.png"); // 默认图标
    }
    
    /**
     * 检查是否包含指定功能的图标
     */
    public static boolean containsFunction(String functionName) {
        return ICON_MAP.containsKey(functionName);
    }
    
    /**
     * 获取所有功能名称
     */
    public static java.util.Set<String> getAllFunctions() {
        return ICON_MAP.keySet();
    }
}