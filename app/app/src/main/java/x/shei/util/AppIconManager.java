package x.shei.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import x.shei.R;

/**
 * 应用图标管理器
 * 统一管理应用中所有图标的加载和显示
 */
public class AppIconManager {

    /**
     * 加载图标到ImageView
     * @param context 上下文
     * @param iconUrl 图标URL
     * @param imageView 目标ImageView
     * @param cornerRadius 圆角半径（dp）
     */
    public static void loadIcon(Context context, String iconUrl, ImageView imageView, int cornerRadius) {
        if (context == null || imageView == null || iconUrl == null) {
            return;
        }

        // 创建圆角变换，将dp转换为px
        int cornerRadiusPx = (int) (context.getResources().getDisplayMetrics().density * cornerRadius);

        RequestOptions requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL) // 使用磁盘缓存
                .centerCrop() // 使用centerCrop以保持图片比例
                .transform(new RoundedCorners(cornerRadiusPx)) // 添加圆角变换
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_error);

        Glide.with(context)
                .load(iconUrl)
                .apply(requestOptions)
                .into(imageView);
    }

    /**
     * 加载图标到ImageView（默认圆角8dp）
     * @param context 上下文
     * @param iconUrl 图标URL
     * @param imageView 目标ImageView
     */
    public static void loadIcon(Context context, String iconUrl, ImageView imageView) {
        loadIcon(context, iconUrl, imageView, 8); // 默认8dp圆角
    }

    /**
     * 根据功能名称加载对应的图标
     * @param context 上下文
     * @param functionName 功能名称
     * @param imageView 目标ImageView
     */
    public static void loadFunctionIcon(Context context, String functionName, ImageView imageView) {
        String iconUrl = FunctionIconMapper.getIconUrl(functionName);
        loadIcon(context, iconUrl, imageView);
    }

    /**
     * 根据功能名称加载对应的图标（带自定义圆角）
     * @param context 上下文
     * @param functionName 功能名称
     * @param imageView 目标ImageView
     * @param cornerRadius 圆角半径（dp）
     */
    public static void loadFunctionIcon(Context context, String functionName, ImageView imageView, int cornerRadius) {
        String iconUrl = FunctionIconMapper.getIconUrl(functionName);
        loadIcon(context, iconUrl, imageView, cornerRadius);
    }
}
