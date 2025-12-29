package x.shei.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import x.shei.util.AppKeepAliveHelper;

/**
 * 守护进程服务
 * 运行在独立进程，用于监控主服务状态并重启
 */
public class GuardService extends Service {
    private static final String TAG = "GuardService";
    private static final int MSG_PING = 1;
    private static final int MSG_REGISTER_CLIENT = 2;
    
    private boolean isMainServiceRunning = false;
    private Handler pingHandler;
    private Runnable pingRunnable;
    private static final long PING_INTERVAL = 5000; // 5秒检查一次
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "GuardService created in process: " + getPackageName() + ":guard");
        
        // 创建ping处理器
        pingHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case MSG_PING:
                        // 收到主服务的ping消息，说明主服务还活着
                        isMainServiceRunning = true;
                        return true;
                    case MSG_REGISTER_CLIENT:
                        // 注册客户端（主服务）的Messenger
                        return true;
                }
                return false;
            }
        });
        
        // 启动ping检查
        startPingCheck();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "GuardService started");
        return START_STICKY; // 服务被杀死后会重启
    }

    @Override
    public IBinder onBind(Intent intent) {
        // 返回Messenger的IBinder，用于与主服务通信
        return new Messenger(pingHandler).getBinder();
    }

    /**
     * 启动ping检查机制
     */
    private void startPingCheck() {
        pingRunnable = new Runnable() {
            @Override
            public void run() {
                // 检查主服务是否还在运行
                if (!isMainServiceRunning) {
                    Log.d(TAG, "检测到主服务已停止，尝试重启...");
                    restartMainService();
                } else {
                    // 重置状态，等待下次ping
                    isMainServiceRunning = false;
                }
                
                // 继续下一次检查
                if (pingHandler != null) {
                    pingHandler.postDelayed(pingRunnable, PING_INTERVAL);
                }
            }
        };
        
        if (pingHandler != null) {
            pingHandler.postDelayed(pingRunnable, PING_INTERVAL);
        }
    }

    /**
     * 重启主服务
     */
    private void restartMainService() {
        try {
            // 尝试启动主服务和无障碍服务
            Intent serviceIntent = new Intent(this, CombinedAccessibilityService.class);
            startService(serviceIntent);
            
            // 同时启动前台保活服务
            Intent keepAliveIntent = new Intent(this, KeepAliveService.class);
            startService(keepAliveIntent);
            
            Log.d(TAG, "已尝试重启主服务");
        } catch (Exception e) {
            Log.e(TAG, "重启主服务失败", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "GuardService destroyed");
        
        if (pingHandler != null && pingRunnable != null) {
            pingHandler.removeCallbacks(pingRunnable);
        }
    }
}