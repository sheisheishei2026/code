package x.shei.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

/**
 * 应用保活辅助类
 * 提供引导用户设置白名单、开启自启动、关闭电池优化等功能
 */
public class AppKeepAliveHelper {

    private static final int REQUEST_CODE_BATTERY_OPTIMIZATION = 1001;
    private static final int REQUEST_CODE_AUTO_START = 1002;
    private static final int REQUEST_CODE_FLOATING_WINDOW = 1003;

    /**
     * 引导用户关闭电池优化
     */
    public static void requestBatteryOptimizationWhitelist(Activity activity) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                activity.startActivityForResult(intent, REQUEST_CODE_BATTERY_OPTIMIZATION);
            } else {
                // Android 6.0以下没有电池优化
                showBatteryOptimizationGuide(activity);
            }
        } catch (Exception e) {
            // 某些设备可能不支持直接跳转，提供通用设置页面
            showBatteryOptimizationGuide(activity);
        }
    }

    /**
     * 针对不同厂商设备提供电池优化白名单设置引导
     */
    private static void showBatteryOptimizationGuide(Activity activity) {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        String message = "为了确保应用能够后台运行和响应音量键，请将应用加入电池优化白名单：\n\n";

        if (manufacturer.contains("huawei") || manufacturer.contains("honor")) {
            message += "华为/荣耀设备：设置 > 应用 > 应用启动管理 > 关闭本应用的自动管理，允许后台活动";
        } else if (manufacturer.contains("xiaomi")) {
            message += "小米设备：设置 > 权限 > 自启动管理 > 打开本应用的自启动权限";
        } else if (manufacturer.contains("oppo")) {
            message += "OPPO设备：设置 > 电池 > 应用耗电管理 > 将本应用设置为不限制";
        } else if (manufacturer.contains("vivo")) {
            message += "VIVO设备：设置 > 电池 > 后台耗电管理 > 打开本应用的后台运行权限";
        } else if (manufacturer.contains("samsung")) {
            message += "三星设备：设置 > 电池 > 应用电源管理 > 关闭本应用的自动休眠";
        } else {
            message += "请在设置中找到电池优化/应用管理/自启动管理，将本应用加入白名单";
        }

        showSettingGuideDialog(activity, "电池优化设置", message);
    }

    /**
     * 引导用户开启自启动
     */
    public static void requestAutoStart(Activity activity) {
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

            activity.startActivityForResult(intent, REQUEST_CODE_AUTO_START);
        } catch (Exception e) {
            // 如果特定的Intent失败，跳转到应用详情页
            openAppDetails(activity);
        }
    }

    /**
     * 引导用户开启悬浮窗权限（用于前台服务通知）
     */
    public static void requestFloatingWindowPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(activity)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, REQUEST_CODE_FLOATING_WINDOW);
            }
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
     * 显示设置引导对话框
     */
    private static void showSettingGuideDialog(Activity activity, String title, String message) {
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openAppDetails(activity);
                    }
                })
                .setNegativeButton("稍后", null)
                .show();
    }

    /**
     * 检查是否已关闭电池优化
     */
//    public static boolean isIgnoringBatteryOptimizations(Context context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            android.app.Application app = (android.app.Application) context.getApplicationContext();
//            android.content.pm.PackageManager pm = app.getPackageManager();
//            return pm.isIgnoringBatteryOptimizations(context.getPackageName());
//        }
//        return true;
//    }

    /**
     * 检查自启动权限（通过检测应用是否被系统限制）
     */
    public static boolean isAutoStartEnabled(Context context) {
        // 由于没有直接的API检查自启动权限，我们只能引导用户手动设置
        // 这里返回true作为默认值，实际应用中可能需要根据具体厂商API判断
        return true;
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

    /**
     * 跳转到无障碍服务设置页面
     */
    public static void openAccessibilitySettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        activity.startActivity(intent);
    }

    /**
     * 完整的保活设置引导流程
     */
    public static void startKeepAliveSetup(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("应用保活设置")
                .setMessage("为了确保应用在后台持续运行并响应音量键，请完成以下设置：\n\n" +
                        "1. 关闭电池优化\n" +
                        "2. 开启自启动权限\n" +
                        "3. 启用无障碍服务")
                .setPositiveButton("开始设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 先引导关闭电池优化
                        requestBatteryOptimizationWhitelist(activity);
                    }
                })
                .setNegativeButton("稍后设置", null)
                .show();
    }
}
