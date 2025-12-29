package x.shei;


import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.alicloud.databox.opensdk.AliyunpanAction;
import com.alicloud.databox.opensdk.AliyunpanBroadcastHelper;
import com.alicloud.databox.opensdk.AliyunpanClient;
import com.alicloud.databox.opensdk.AliyunpanClientConfig;
import com.google.gson.Gson;
import com.tencent.smtt.sdk.QbSdk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import x.shei.util.CrashHandler;

public class App extends Application {
    static Context mContext;

//    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (AliyunpanAction.NOTIFY_LOGIN_SUCCESS.equals(action)) {
//                // 授权成功处理逻辑
//            } else if (AliyunpanAction.NOTIFY_RESET_STATUS.equals(action)) {
//                // 清除授权处理逻辑
//            }
//        }
//    };

//    @Override
//    protected void attachBaseContext(Context base) {
//        super.attachBaseContext(base);
////        Log.e("asd", "attachBaseContext : " + 1);
//        try {
//            hookClipboardService(this);
////            hookActivityThreadInstrumentation();
//        } catch (Exception e) {
////            Log.e("asd", "2 : " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//    public static AliyunpanClient aliyunpanClient;

    public static AliyunpanClient aliyunpanClient;

    public static void initApp(Context context) {
        AliyunpanClientConfig config = new AliyunpanClientConfig.Builder(context, "35d59e2184564c29966d1c44b2284bac")
                .appendScope(AliyunpanClientConfig.SCOPE_FILE_WRITE)
                .appLevel(AliyunpanClientConfig.AppLevel.DEFAULT)
                .autoLogin()
                .downFolder(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Aliyunpan"))
                .build();
        aliyunpanClient = AliyunpanClient.init(config);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        initApp(this);
        QbSdk.initX5Environment(this, new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                // 内核初始化完成，可能为系统内核，也可能为系统内核
            }
            /**
             * 预初始化结束
             * 由于X5内核体积较大，需要依赖网络动态下发，所以当内核不存在的时候，默认会回调false，此时将会使用系统内核代替
             * @param isX5 是否使用X5内核
             */
            @Override
            public void onViewInitFinished(boolean isX5) {

            }
        });

        // 初始化全局异常捕获
        CrashHandler.getInstance().init(this);

//        IntentFilter filter = new IntentFilter();
//        filter.addAction(AliyunpanAction.NOTIFY_LOGIN_SUCCESS);
//        filter.addAction(AliyunpanAction.NOTIFY_RESET_STATUS);
//        registerReceiver(mReceiver, filter);
//        AliyunpanApp.initApp(this, appId);


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                if (placesData == null) {
//                    String jsonData = loadJSONFromAsset(mContext);
//                    if (jsonData == null) {
//                        return;
//                    }
//                    try {
//                        Gson gson = new Gson();
//                        placesData = gson.fromJson(jsonData, MapActivityBack.PlacesData.class);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
    }

//    public static void initApp(Context context, String appId) {
//        // 配置
//        AliyunpanClientConfig config = new AliyunpanClientConfig.Builder(context, appId)
//                .build();
//        // 初始化client
//        aliyunpanClient = AliyunpanClient.init(config);
//    }

    public void copyfile(String filepath, String fileName, String assetsName) {
        try {
            Log.e("asd", "filepath : " + filepath);
            File file = new File(filepath + "/" + fileName);
            if (!file.exists()) {
                InputStream is = getResources().getAssets().open(assetsName);
                FileOutputStream fos = new FileOutputStream(filepath + "/" + fileName);
                byte[] buffer = new byte[7168];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Context getApplication() {
        return mContext;
    }

//    public void hookClipboardService(final Context context) throws Exception {
//        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
//
//        // 1. 获取mService对象
//        Class<?> locationManagerClazsz = Class.forName("android.content.ClipboardManager");
//        for(int i=0; i<locationManagerClazsz.getFields().length ; i++){
//            Log.e("asd", "aaa : " + locationManagerClazsz.getFields()[i]);
//        }
//        Field mServiceFiled = locationManagerClazsz.getField("mService");
//        mServiceFiled.setAccessible(true);
//        final Object mService = mServiceFiled.get(clipboardManager);
//
//        // 2. 初始化动态代理对象，增加自定义方法逻辑
//        Class aClass = Class.forName("android.content.IClipboard");
//        Object proxyInstance = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{aClass}, new InvocationHandler() {
//
//            @Override
//            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                String name = method.getName();
//                if ("getPrimaryClip".equals(name)) {
//                    // 执行计数逻辑
//                    Log.e("asd", "拦截getPrimaryClip : " + 1);
//                }
//                return method.invoke(mService, args);
//            }
//        });
//
//        // 3. 偷梁换柱，替换系统的mService
//        Field sServiceField = ClipboardManager.class.getDeclaredField("mService");
//        sServiceField.setAccessible(true);
//        sServiceField.set(clipboardManager, proxyInstance);
//    }

    private void hookActivityThreadInstrumentation() throws Exception {
        // 1.获取ActivityThread的单例对象sCurrentActivityThread
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Field activityThreadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
        activityThreadField.setAccessible(true);

        // 2.获取ActivityThread的成员变量mInstrumentation
        Object activityThread = activityThreadField.get(null);
        Field instrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
        instrumentationField.setAccessible(true);

        // 3.将mInstrumentation替换成代理对象InstrumentationProxy
        InstrumentationProxy proxy = new InstrumentationProxy();
        instrumentationField.set(activityThread, proxy);
        Log.e("asd", "11111 : " + 1);
    }

    public class InstrumentationProxy extends Instrumentation {
        String currentPageName;

        public Activity newActivity(Class<?> clazz, Context context, IBinder token, Application application, Intent intent, ActivityInfo info, CharSequence title, Activity parent, String id, Object lastNonConfigurationInstance) throws InstantiationException, IllegalAccessException {
            ComponentName componentName = intent.getComponent();
            Class activityClass = componentName.getClass();
            currentPageName = activityClass.getSimpleName();
//            Metrics.getInstance().recordLaunchStep(currentPageName + ".launch");
            return super.newActivity(clazz, context, token, application, intent, info, title, parent, id, lastNonConfigurationInstance);
        }

        public void callActivityOnCreate(Activity activity, Bundle icicle) {
//            Metrics.getInstance().recordLaunchStep(currentPageName + ".onCreate+");
            super.callActivityOnCreate(activity, icicle);
            Log.e("asd", "callActivityOnCreate : " + 1);
//            Metrics.getInstance().recordLaunchStep(currentPageName + ".onCreate-");
        }

        public void callActivityOnResume(final Activity activity) {
            super.callActivityOnResume(activity);
            Log.e("asd", "callActivityOnResume : " + 1);
        }

        public void callActivityOnStart(Activity activity) {
            super.callActivityOnStart(activity);
            Log.e("asd", "callActivityOnStart : " + 1);
        }

//        public static void hookLocationManager(LocationManager locationManager) {
//            try {
//                Object iLocationManager = null;
//                Class<?> locationManagerClazsz = Class.forName("android.location.LocationManager");        //获取LocationManager的mService成员
//                iLocationManager = getField(locationManagerClazsz, locationManager, "mService");
//                Class<?> iLocationManagerClazz = Class.forName("android.location.ILocationManager");
//                Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
//                        new Class<?>[]{iLocationManagerClazz}, new ILocationManagerProxy(iLocationManager));
//                setField(locationManagerClazsz, locationManager, "mService", proxy);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }

}
