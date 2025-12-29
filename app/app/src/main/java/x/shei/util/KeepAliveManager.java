package x.shei.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Toast;

/**
 * 保活管理器
 * 提供独立的保活设置功能
 */
public class KeepAliveManager {

    /**
     * 请求关闭电池优化
     */
    public static void requestBatteryOptimizationWhitelist(Activity activity) {
        if (isIgnoringBatteryOptimizations(activity)) {
            Toast.makeText(activity, "电池优化已关闭", Toast.LENGTH_SHORT).show();
        } else {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    activity.startActivity(intent);
                }
            } catch (Exception e) {
                // 某些设备可能不支持直接跳转，提供通用设置页面
                openAppDetails(activity);
            }
        }
    }

    /**
     * 请求后台运行权限
     */
    public static void requestBackgroundRunning(Activity activity) {
        Toast.makeText(activity, "请在设置中开启后台运行权限", Toast.LENGTH_LONG).show();

        try {
            Intent intent = new Intent();
            String manufacturer = Build.MANUFACTURER.toLowerCase();

            if (manufacturer.contains("huawei")) {
                intent.setComponent(new android.content.ComponentName("com.huawei.systemmanager",
                        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"));
            } else if (manufacturer.contains("xiaomi")) {
                intent.setComponent(new android.content.ComponentName("com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            } else if (manufacturer.contains("oppo")) {
                intent.setComponent(new android.content.ComponentName("com.coloros.safecenter",
                        "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
            } else if (manufacturer.contains("vivo")) {
                intent.setComponent(new android.content.ComponentName("com.iqoo.secure",
                        "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"));
            } else if (manufacturer.contains("samsung")) {
                intent.setComponent(new android.content.ComponentName("com.samsung.android.sm",
                        "com.samsung.android.sm.app.dashboard.SmartManagerDashBoardActivity"));
            } else if (manufacturer.contains("meizu")) {
                intent.setComponent(new android.content.ComponentName("com.meizu.safe",
                        "com.meizu.safe.permission.SmartBGActivity"));
            } else {
                // 通用方法
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
            }

            activity.startActivity(intent);
        } catch (Exception e) {
            // 如果特定的Intent失败，跳转到应用详情页
            openAppDetails(activity);
        }
    }

    /**
     * 请求自启动权限
     */
    public static void requestAutoStart(Activity activity) {
        Toast.makeText(activity, "请在设置中开启自启动权限", Toast.LENGTH_LONG).show();

        try {
            Intent intent = new Intent();
            String manufacturer = Build.MANUFACTURER.toLowerCase();

            if (manufacturer.contains("huawei")) {
                intent.setComponent(new android.content.ComponentName("com.huawei.systemmanager",
                        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"));
            } else if (manufacturer.contains("xiaomi")) {
                intent.setComponent(new android.content.ComponentName("com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            } else if (manufacturer.contains("oppo")) {
                intent.setComponent(new android.content.ComponentName("com.coloros.safecenter",
                        "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
            } else if (manufacturer.contains("vivo")) {
                intent.setComponent(new android.content.ComponentName("com.iqoo.secure",
                        "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"));
            } else if (manufacturer.contains("samsung")) {
                intent.setComponent(new android.content.ComponentName("com.samsung.android.sm",
                        "com.samsung.android.sm.app.dashboard.SmartManagerDashBoardActivity"));
            } else if (manufacturer.contains("meizu")) {
                intent.setComponent(new android.content.ComponentName("com.meizu.safe",
                        "com.meizu.safe.permission.SmartBGActivity"));
            } else {
                // 通用方法
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
            }

            activity.startActivity(intent);
        } catch (Exception e) {
            // 如果特定的Intent失败，跳转到应用详情页
            openAppDetails(activity);
        }
    }

    /**
     * 检查应用是否在后台运行白名单中
     * 注意：这是一个近似判断，因为没有统一的API检查后台运行权限
     */
    public static boolean isBackgroundRunningAllowed(Context context) {
        // 由于没有统一的API检查后台运行权限，这里只是提供一个近似的判断
        // 实际上需要用户手动检查设置
        return true; // 默认返回true，因为无法准确判断
    }

    /**
     * 跳转到无障碍服务设置页面
     */
    public static void openAccessibilitySettings(Activity activity) {
        if (isAccessibilityServiceEnabled(activity, x.shei.service.CombinedAccessibilityService.class)) {
            Toast.makeText(activity, "无障碍服务已开启", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            activity.startActivity(intent);
        }
    }

    /**
     * 跳转到应用详情页面
     */
    public static void openAppDetails(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    /**
     * 获取所有保活状态
     */
    public static KeepAliveStatus getKeepAliveStatus(Context context) {
        return new KeepAliveStatus(
            isIgnoringBatteryOptimizations(context),
            isBackgroundRunningAllowed(context),
            isAutoStartEnabled(context),
            isAccessibilityServiceEnabled(context, x.shei.service.CombinedAccessibilityService.class)
        );
    }


    public static boolean isIgnoringBatteryOptimizations(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // API 23+
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return true; // 低于 Android 6.0 不受电池优化限制
    }

    /**
     * 检查自启动是否已启用
     * 注意：这是一个近似判断，因为没有统一的API检查自启动权限
     */
    public static boolean isAutoStartEnabled(Context context) {
        // 由于没有统一的API检查自启动权限，这里只是提供一个近似的判断
        // 实际上需要用户手动检查设置
        return true; // 默认返回true，因为无法准确判断
    }

    /**
     * 检查无障碍服务是否已启用
     */
    public static boolean isAccessibilityServiceEnabled(Context context, Class<?> serviceClass) {
        String service = context.getPackageName() + "/" + serviceClass.getCanonicalName();
        String enabledServices = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        return enabledServices != null && enabledServices.contains(service);
    }
}
