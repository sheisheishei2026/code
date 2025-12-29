package x.shei.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import x.shei.R;
import x.shei.activity.APPActivity;

/**
 * 前台服务保活机制
 * 通过前台服务提高应用存活率
 */
public class KeepAliveService extends Service {
    private static final String TAG = "KeepAliveService";
    private static final String CHANNEL_ID = "KeepAliveChannel";
    private static final int NOTIFICATION_ID = 1001;

    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "KeepAliveService created");
        createNotificationChannel();
        acquireWakeLock();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "KeepAliveService started");
        startForeground(NOTIFICATION_ID, createNotification());

        // 保持服务运行
        return START_STICKY; // 服务被杀死后会尝试重启
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "KeepAliveService destroyed");

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    /**
     * 创建通知渠道（Android 8.0+）
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "应用保活服务",
                        NotificationManager.IMPORTANCE_LOW
                );
                channel.setDescription("保持应用在后台运行的服务");
                channel.setShowBadge(false); // 不在应用图标上显示通知小红点
                nm.createNotificationChannel(channel);
            }
        }
    }

    /**
     * 创建前台服务通知
     */
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, APPActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("应急助手服务")
                .setContentText("正在后台运行，随时响应音量键")
                .setSmallIcon(R.drawable.ic_launcher) // 使用应用图标
                .setContentIntent(pendingIntent)
                .setOngoing(true) // 设置为持续通知
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    /**
     * 获取唤醒锁，防止CPU休眠
     */
    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "KeepAliveService::WakeLock"
            );
            wakeLock.acquire(10*60*1000L /*10 minutes*/); // 持续10分钟，避免过度耗电
        }
    }

    /**
     * 重新获取唤醒锁
     */
    public void reacquireWakeLock() {
        if (wakeLock != null && !wakeLock.isHeld()) {
            acquireWakeLock();
        }
    }
}
