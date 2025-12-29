package x.shei.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 系统事件接收器
 * 监听开机启动、应用被清理等事件，用于重启服务
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "收到广播: " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            "android.intent.action.QUICKBOOT_POWERON".equals(action) ||
            "android.intent.action.MY_PACKAGE_REPLACED".equals(action)) {
            // 开机启动或应用更新后重启服务
            restartServices(context);
        } else if ("android.intent.action.PACKAGE_RESTARTED".equals(action) &&
                   intent.getDataString() != null &&
                   intent.getDataString().contains(context.getPackageName())) {
            // 应用被重启后重启服务
            restartServices(context);
        }
    }

    /**
     * 重启服务
     */
    private void restartServices(Context context) {
        try {
            Log.d(TAG, "尝试重启服务...");
            
            // 启动无障碍服务
            Intent accessibilityIntent = new Intent(context, CombinedAccessibilityService.class);
            accessibilityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(accessibilityIntent);
            
            // 启动前台保活服务
            Intent keepAliveIntent = new Intent(context, KeepAliveService.class);
            keepAliveIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(keepAliveIntent);
            
            // 启动守护服务
            Intent guardIntent = new Intent(context, GuardService.class);
            guardIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(guardIntent);
            
            Log.d(TAG, "服务重启命令已发送");
        } catch (Exception e) {
            Log.e(TAG, "重启服务时出错", e);
        }
    }
}