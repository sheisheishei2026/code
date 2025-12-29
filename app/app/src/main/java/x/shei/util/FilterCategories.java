package x.shei.util;

import java.util.Arrays;
import java.util.List;

public class FilterCategories {
    // 左侧蓝色区域分类
    public static final List<String> AREA_CATEGORIES = Arrays.asList(
            "四合院",
            "看巨幕",
            "去书苑",
            "看夜景",
            "爬长城",
            "五环外",
            "六环外",
            "逛教堂",
            "逛胡同",
            "逛美术馆",
            "名人故居",
            "运动健身",
            "民俗文化",
            "夏日时光",
            "秋日银杏",
            "地铁直达"
    );

    // 中间紫色功能分类
    public static final List<String> FUNCTION_CATEGORIES = Arrays.asList(
            "全部",
            "免费",
            "收费",
            "换票",
            "持证",
            "有VR",
            "有视频",
            "有公众号",
            "有小程序",
            "三星以上",
            "讲解表演",
            "需要预约",
            "工作日开",
            "小吃饭店",
            "5A级景区"
    );

    // 右侧绿色类型分类
    public static final List<String> TYPE_CATEGORIES = Arrays.asList(
            "人文·历史·社会",
            "艺术·展馆·书苑",
            "行业·科学·技术",
            "寺庙·坛观·宗教",
            "学院·大学·景点",
            "商场·商圈·零售",
            "园区·大厦·酒店",
            "公园·山水·自然",
            "街道·美食·小吃"
    );

    // 颜色值
    public static final int COLOR_AREA = 0xFF4B8BF4;      // 蓝色
    public static final int COLOR_FUNCTION = 0xFF9C27B0;  // 紫色
    public static final int COLOR_TYPE = 0xFF4CAF50;      // 绿色
}
