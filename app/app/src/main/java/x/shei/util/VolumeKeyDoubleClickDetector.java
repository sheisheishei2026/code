package x.shei.util;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import x.shei.activity.EmergencyActivity;
import x.shei.activity.SoundEffectsActivity;

public class VolumeKeyDoubleClickDetector {
    private static final int DOUBLE_CLICK_TIME_DELTA = 300; // 双击时间间隔，单位毫秒
    private static long lastClickTime = 0;
    private static int lastKeyCode = -1; // 记录上一次按键的键码

    public static boolean isDoubleClick(Context context, int keyCode) {
        long clickTime = System.currentTimeMillis();

        // 只对音量加键进行双击检测
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA && lastKeyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                // 启动音效页面
                Intent intent = new Intent(context, EmergencyActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                lastClickTime = 0; // 重置双击计时，避免连续触发
                lastKeyCode = -1;
                return true;
            }

            lastClickTime = clickTime;
            lastKeyCode = keyCode;
        } else {
            // 如果是其他按键，重置计时器
            lastClickTime = 0;
            lastKeyCode = -1;
        }

        return false;
    }

    public static boolean isVolumeUpDoubleClick(Context context) {
        return isDoubleClick(context, KeyEvent.KEYCODE_VOLUME_UP);
    }

    public static boolean isVolumeDownDoubleClick(Context context) {
        return isDoubleClick(context, KeyEvent.KEYCODE_VOLUME_DOWN);
    }

    // 重置双击计时器，用于长按后重置状态
    public static void resetClickTimer() {
        lastClickTime = 0;
        lastKeyCode = -1;
    }

    // 检查是否在双击时间窗口内
    public static boolean isInDoubleClickWindow(int keyCode) {
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA && lastKeyCode == keyCode);
    }
}
