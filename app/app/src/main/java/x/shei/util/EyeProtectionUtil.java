package x.shei.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

/**
 * 系统级护眼模式工具类
 * 使用系统悬浮窗在所有应用上方显示暖色调滤镜
 */
public class EyeProtectionUtil {

    private static final String PREF_NAME = "eye_protection";
    private static final String KEY_ENABLED = "eye_protection_enabled";
    private static final String KEY_FILTER_OPACITY = "eye_protection_filter_opacity";

    // 默认值
    private static final float DEFAULT_FILTER_OPACITY = 0.3f; // 30%不透明度

    private static View filterView;
    private static WindowManager windowManager;
    private static WindowManager.LayoutParams layoutParams;

    /**
     * 检查是否有悬浮窗权限
     */
    public static boolean hasOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    /**
     * 请求悬浮窗权限
     */
    public static void requestOverlayPermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(activity)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, requestCode);
            }
        }
    }

    /**
     * 检查护眼模式是否启用
     */
    public static boolean isEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ENABLED, false);
    }

    /**
     * 启用系统级护眼模式
     */
    public static boolean enable(Context context) {
        if (!hasOverlayPermission(context)) {
            return false;
        }

        if (filterView != null) {
            return true; // 已经启用
        }

        try {
            windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            if (windowManager == null) {
                return false;
            }

            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            float opacity = prefs.getFloat(KEY_FILTER_OPACITY, DEFAULT_FILTER_OPACITY);

            // 创建暖色调滤镜视图
            filterView = new View(context.getApplicationContext());
            int filterColor = Color.argb(
                    (int) (opacity * 255), // 不透明度
                    255,  // 红色
                    220,  // 绿色（稍微降低，产生暖色调）
                    180   // 蓝色（大幅降低，减少蓝光）
            );
            filterView.setBackgroundColor(filterColor);

            // 设置窗口参数 - 使用更高层级以覆盖状态栏和系统界面
            int type;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
            } else {
                type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }

            layoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    type,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                            | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                            | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    PixelFormat.TRANSLUCENT
            );
            
            // 设置窗口位置，从屏幕最顶部开始（包括状态栏）
            layoutParams.gravity = Gravity.TOP | Gravity.START;
            layoutParams.x = 0;
            layoutParams.y = 0; // 从状态栏顶部开始
            
            // 确保覆盖状态栏区域
            layoutParams.format = PixelFormat.TRANSLUCENT;
            
            // 对于Android 8.0+，TYPE_APPLICATION_OVERLAY 已经足够覆盖状态栏
            // 但需要确保窗口大小和位置正确

            // 添加到窗口管理器
            windowManager.addView(filterView, layoutParams);

            // 保存状态
            prefs.edit().putBoolean(KEY_ENABLED, true).apply();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 禁用系统级护眼模式
     */
    public static void disable(Context context) {
        if (filterView == null || windowManager == null) {
            return; // 已经禁用
        }

        try {
            windowManager.removeView(filterView);
            filterView = null;
            windowManager = null;
            layoutParams = null;

            // 保存状态
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .edit().putBoolean(KEY_ENABLED, false).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 切换护眼模式
     */
    public static boolean toggle(Context context) {
        if (filterView == null) {
            return enable(context);
        } else {
            disable(context);
            return true;
        }
    }

    /**
     * 更新滤镜不透明度
     */
    public static void updateOpacity(Context context) {
        if (filterView == null) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        float opacity = prefs.getFloat(KEY_FILTER_OPACITY, DEFAULT_FILTER_OPACITY);

        int filterColor = Color.argb(
                (int) (opacity * 255),
                255,
                220,
                180
        );
        filterView.setBackgroundColor(filterColor);
    }

    /**
     * 设置滤镜不透明度（0.0 - 1.0）
     */
    public static void setFilterOpacity(Context context, float opacity) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putFloat(KEY_FILTER_OPACITY, opacity).apply();
        
        // 如果已启用，立即更新
        if (filterView != null) {
            updateOpacity(context);
        }
    }

    /**
     * 获取当前滤镜不透明度设置
     */
    public static float getFilterOpacity(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat(KEY_FILTER_OPACITY, DEFAULT_FILTER_OPACITY);
    }
}
