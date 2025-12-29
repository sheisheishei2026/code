package x.shei.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import x.shei.activity.APPActivity;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private static CrashHandler INSTANCE;
    private Context context;
    private Thread.UncaughtExceptionHandler defaultHandler;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CrashHandler();
        }
        return INSTANCE;
    }

    public void init(Context context) {
        this.context = context;
        // 获取系统默认的异常处理器
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置当前类为主线程的默认异常处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && defaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            defaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Error : ", e);
            }
            // 重启应用
            Intent intent = new Intent(context, APPActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            Process.killProcess(Process.myPid());
            System.exit(1);
        }
    }

    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }

        try {
            // 保存错误报告文件
            saveCrashInfo(ex);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error saving crash info", e);
            return false;
        }
    }

    private void saveCrashInfo(Throwable ex) {
        try {
            StringBuffer sb = new StringBuffer();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String time = formatter.format(new Date());

            sb.append("Time: ").append(time).append("\n\n");

            // 收集设备信息
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            sb.append("App Version: ").append(pi.versionName).append("_").append(pi.versionCode).append("\n");
            sb.append("OS Version: ").append(Build.VERSION.RELEASE).append("_").append(Build.VERSION.SDK_INT).append("\n");
            sb.append("Device: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n\n");

            // 获取错误信息
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            printWriter.close();
            sb.append("Exception: \n").append(writer.toString());

            // 保存文件
            String fileName = "crash_" + time.replace(" ", "_").replace(":", "-") + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File dir = new File(context.getExternalFilesDir(null), "crash");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(new File(dir, fileName));
                fos.write(sb.toString().getBytes());
                fos.close();
            }

            // 打印到日志
            Log.e(TAG, sb.toString());

        } catch (Exception e) {
            Log.e(TAG, "Error saving crash info", e);
        }
    }
}
