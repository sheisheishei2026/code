package x.shei.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import x.shei.activity.EmergencyActivity;
import x.shei.activity.SoundEffectsActivity;

import x.shei.util.VolumeKeyDoubleClickDetector;

/**
 * 组合无障碍服务：包含抖音自动滑动和音量键双击监听功能
 */
public class CombinedAccessibilityService extends AccessibilityService {
    private static final String TAG = "CombinedAccessibility";
    private static final String DOUYIN_PACKAGE = "com.ss.android.ugc.aweme.lite"; // 抖音极速版包名
    private static final long SCROLL_INTERVAL = 10000; // 10秒滑动一次
    private static final float SCROLL_PERCENT = 0.8f; // 滑动屏幕的80%
    private static final String PREFS_NAME = "douyin_scroll_prefs";
    private static final String KEY_AUTO_SCROLL = "auto_scroll_enabled";

    private Handler handler;
    private Runnable scrollRunnable;
    private boolean isScrolling = false;
    private static CombinedAccessibilityService instance;

    // 音量键长按检测相关变量
    private static final int LONG_PRESS_DELAY = 1000; // 1秒
    private Handler volumeKeyHandler = new Handler(Looper.getMainLooper());
    private Runnable volumeKeyRunnable;
    private boolean isVolumeUpPressed = false;
    private boolean isVolumeDownPressed = false;
    
    // 守护进程相关变量
    private Messenger guardMessenger;
    private boolean isBound = false;
    private static final int MSG_PING = 1;
    private static final int MSG_REGISTER_CLIENT = 2;
    private Runnable pingRunnable;
    private Handler pingHandler;
    private static final long PING_INTERVAL = 3000; // 3秒ping一次守护进程

    /**
     * 设置是否启用自动滑动
     */
    public static void setAutoScrollEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_AUTO_SCROLL, enabled).apply();
        if (instance != null) {
            if (enabled) {
                instance.startAutoScroll();
            } else {
                instance.stopAutoScroll();
            }
        }
    }

    /**
     * 检查是否启用了自动滑动
     */
    public static boolean isAutoScrollEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_AUTO_SCROLL, false);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 不需要处理事件，只需要执行手势
    }

    @Override
    public void onInterrupt() {
        stopAutoScroll();
        Log.d(TAG, "服务被中断");
    }

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        super.onKeyEvent(event);
        
        Context context = getApplicationContext();
        if (context == null) {
            return false;
        }
        
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    handleVolumeUpPress(context);
                    return false; // 不拦截，让系统处理音量调节
                } else if (action == KeyEvent.ACTION_UP) {
                    handleVolumeUpRelease();
                    // 检查是否是双击，如果是则打开音效页面
                    VolumeKeyDoubleClickDetector.isVolumeUpDoubleClick(context);
                    return false; // 不拦截，让系统处理音量调节
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    handleVolumeDownPress(context);
                    return false; // 不拦截，让系统处理音量调节
                } else if (action == KeyEvent.ACTION_UP) {
                    handleVolumeDownRelease();
                    // 音量减键只处理长按，不处理双击，直接返回false让系统处理音量调节
                    return false; // 不拦截，让系统处理音量调节
                }
                break;
        }
        
        return false;
    }
    
    private void handleVolumeUpPress(Context context) {
        if (!isVolumeUpPressed) {
            isVolumeUpPressed = true;
            // 设置长按检测的Runnable
            volumeKeyRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isVolumeUpPressed) {
                        // 长按音量上键超过1秒，跳转到EmergencyActivity
                        Intent intent = new Intent(context, EmergencyActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                        VolumeKeyDoubleClickDetector.resetClickTimer(); // 长按后重置双击计时器
                    }
                }
            };
            // 延迟执行Runnable
            volumeKeyHandler.postDelayed(volumeKeyRunnable, LONG_PRESS_DELAY);
        }
    }
    
    private void handleVolumeUpRelease() {
        if (isVolumeUpPressed) {
            isVolumeUpPressed = false;
            // 取消长按检测的Runnable
            if (volumeKeyRunnable != null) {
                volumeKeyHandler.removeCallbacks(volumeKeyRunnable);
            }
            // 双击检测在onKeyEvent方法中处理
        }
    }
    
    private void handleVolumeDownPress(Context context) {
        if (!isVolumeDownPressed) {
            isVolumeDownPressed = true;
            // 设置长按检测的Runnable
            volumeKeyRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isVolumeDownPressed) {
                        // 长按音量下键超过1秒，也可以执行相应操作
                        // 目前只处理音量上键长按跳转到EmergencyActivity
                        VolumeKeyDoubleClickDetector.resetClickTimer(); // 长按后重置双击计时器
                    }
                }
            };
            // 延迟执行Runnable
            volumeKeyHandler.postDelayed(volumeKeyRunnable, LONG_PRESS_DELAY);
        }
    }
    
    private void handleVolumeDownRelease() {
        if (isVolumeDownPressed) {
            isVolumeDownPressed = false;
            // 取消长按检测的Runnable
            if (volumeKeyRunnable != null) {
                volumeKeyHandler.removeCallbacks(volumeKeyRunnable);
            }
            // 音量减键不处理双击，只处理长按，直接返回让系统处理音量调节
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        handler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "无障碍服务已连接");
        
        // 检查是否需要自动开始滑动
        if (isAutoScrollEnabled(this)) {
            startAutoScroll();
        }
            
        // 绑定守护进程服务
        bindGuardService();
        
        // 启动ping机制
        startPingMechanism();
    }

    /**
     * 开始自动滑动
     */
    public void startAutoScroll() {
        if (isScrolling) {
            Log.d(TAG, "已经在滑动中");
            return;
        }

        isScrolling = true;
        scrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (isScrolling) {
                    performScroll();
                    handler.postDelayed(this, SCROLL_INTERVAL);
                }
            }
        };
        
        // 立即执行第一次滑动
        handler.postDelayed(scrollRunnable, 1000); // 延迟1秒后开始第一次滑动
        Log.d(TAG, "开始自动滑动");
    }

    /**
     * 停止自动滑动
     */
    public void stopAutoScroll() {
        isScrolling = false;
        if (handler != null && scrollRunnable != null) {
            handler.removeCallbacks(scrollRunnable);
        }
        Log.d(TAG, "停止自动滑动");
    }

    /**
     * 执行向上滑动手势
     */
    private void performScroll() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.w(TAG, "当前Android版本不支持手势服务");
            return;
        }

        // 获取屏幕尺寸 - 使用WindowManager而不是getDisplay()
        android.graphics.Point screenSize = new android.graphics.Point();
        try {
            android.view.WindowManager windowManager = (android.view.WindowManager) getSystemService(android.content.Context.WINDOW_SERVICE);
            if (windowManager != null && windowManager.getDefaultDisplay() != null) {
                windowManager.getDefaultDisplay().getRealSize(screenSize);
            } else {
                // 如果获取不到，使用默认值
                screenSize.x = 1080;
                screenSize.y = 1920;
            }
        } catch (Exception e) {
            Log.e(TAG, "获取屏幕尺寸失败，使用默认值", e);
            screenSize.x = 1080;
            screenSize.y = 1920;
        }

        int screenWidth = screenSize.x;
        int screenHeight = screenSize.y;

        // 计算滑动路径：从下往上滑一屏的80%
        // 从屏幕中间水平位置开始，从底部向上滑动80%屏幕高度
        int startX = screenWidth / 2;
        // 从屏幕底部向上20%位置开始（即屏幕高度的80%位置，y坐标从顶部开始计算）
        int startY = (int) (screenHeight * 0.8f);
        // 向上滑动80%屏幕高度
        int scrollDistance = (int) (screenHeight * SCROLL_PERCENT);
        int endY = Math.max(0, startY - scrollDistance); // 确保不超出屏幕顶部

        Path path = new Path();
        path.moveTo(startX, startY);
        path.lineTo(startX, endY);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(
                path, 0, 300); // 300ms完成滑动
        builder.addStroke(stroke);

        GestureDescription gesture = builder.build();
        dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d(TAG, "滑动完成: (" + startX + ", " + startY + ") -> (" + startX + ", " + endY + ")");
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.w(TAG, "滑动被取消");
            }
        }, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAutoScroll();
        instance = null;
        
        // 解绑守护进程服务
        unbindGuardService();
        
        Log.d(TAG, "服务已销毁");
    }
    
    /**
     * 绑定守护进程服务
     */
    private void bindGuardService() {
        Intent intent = new Intent(this, GuardService.class);
        intent.setPackage(getPackageName()); // 确保在同一应用内
        boolean result = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        if (result) {
            Log.d(TAG, "成功绑定守护服务");
        } else {
            Log.e(TAG, "绑定守护服务失败");
        }
    }
    
    /**
     * 解绑守护进程服务
     */
    private void unbindGuardService() {
        if (isBound) {
            try {
                unbindService(serviceConnection);
                isBound = false;
                Log.d(TAG, "已解绑守护服务");
            } catch (Exception e) {
                Log.e(TAG, "解绑守护服务时出错", e);
            }
        }
    }
    
    /**
     * 服务连接回调
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            guardMessenger = new Messenger(service);
            isBound = true;
            Log.d(TAG, "已连接到守护服务");
            
            // 向守护服务注册自己
            registerToGuardService();
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            guardMessenger = null;
            isBound = false;
            Log.d(TAG, "与守护服务断开连接");
            
            // 尝试重新绑定
            bindGuardService();
        }
    };
    
    /**
     * 向守护服务注册
     */
    private void registerToGuardService() {
        if (guardMessenger != null && isBound) {
            try {
                Message msg = Message.obtain(null, MSG_REGISTER_CLIENT);
                msg.replyTo = new Messenger(new Handler(Looper.getMainLooper(), new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        return false; // 暂时不需要处理守护服务的回复
                    }
                }));
                guardMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "向守护服务注册失败", e);
            }
        }
    }
    
    /**
     * 启动ping机制，定期向守护进程发送心跳
     */
    private void startPingMechanism() {
        pingHandler = new Handler(Looper.getMainLooper());
        pingRunnable = new Runnable() {
            @Override
            public void run() {
                sendPingToGuard();
                
                // 继续下一次ping
                if (pingHandler != null) {
                    pingHandler.postDelayed(this, PING_INTERVAL);
                }
            }
        };
        
        if (pingHandler != null) {
            pingHandler.postDelayed(pingRunnable, PING_INTERVAL);
        }
    }
    
    /**
     * 向守护服务发送ping消息
     */
    private void sendPingToGuard() {
        if (guardMessenger != null && isBound) {
            try {
                Message msg = Message.obtain(null, MSG_PING);
                guardMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "发送ping到守护服务失败", e);
            }
        }
    }
}