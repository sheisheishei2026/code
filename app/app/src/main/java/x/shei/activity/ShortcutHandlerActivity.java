package x.shei.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import x.shei.R;

public class ShortcutHandlerActivity extends AppCompatActivity {
    
    public static final String ACTION_OPEN_SETTINGS = "x.shei.action.OPEN_SETTINGS";
    public static final String ACTION_OPEN_PHONE_MANAGER = "x.shei.action.OPEN_PHONE_MANAGER";
    public static final String ACTION_OPEN_QINLIN_DOOR = "x.shei.action.OPEN_QINLIN_DOOR";
    
    private static final String PACKAGE_PHONE_MANAGER = "com.meizu.safe";
    private static final String PACKAGE_QINLIN_DOOR = "com.qinlin.edoor";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String action = getIntent().getAction();
        if (action == null) {
            finish();
            return;
        }
        
        switch (action) {
            case ACTION_OPEN_SETTINGS:
                openSettings();
                break;
            case ACTION_OPEN_PHONE_MANAGER:
                openPhoneManager();
                break;
            case ACTION_OPEN_QINLIN_DOOR:
                openQinlinDoor();
                break;
            default:
                finish();
                break;
        }
    }
    
    private void openSettings() {
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "无法打开设置", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
    
    private void openPhoneManager() {
        try {
            PackageManager pm = getPackageManager();
            
            // 首先检查应用是否安装
            try {
                pm.getPackageInfo(PACKAGE_PHONE_MANAGER, 0);
                Log.d("ShortcutHandler", "手机管家已安装: " + PACKAGE_PHONE_MANAGER);
            } catch (PackageManager.NameNotFoundException e) {
                Log.d("ShortcutHandler", "手机管家未安装: " + PACKAGE_PHONE_MANAGER);
                Toast.makeText(this, "未安装手机管家", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            // 方法1: 尝试使用 getLaunchIntentForPackage（最常用）
            Intent intent = pm.getLaunchIntentForPackage(PACKAGE_PHONE_MANAGER);
            if (intent != null) {
                Log.d("ShortcutHandler", "使用方法1启动手机管家");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return;
            }
            
            // 方法2: 查询并启动主Activity
            try {
                Intent mainIntent = pm.getLaunchIntentForPackage(PACKAGE_PHONE_MANAGER);
                if (mainIntent == null) {
                    // 如果getLaunchIntentForPackage返回null，尝试查询主Activity
                    android.content.pm.ApplicationInfo appInfo = pm.getApplicationInfo(PACKAGE_PHONE_MANAGER, 0);
                    android.content.pm.ResolveInfo resolveInfo = pm.resolveActivity(
                        new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(PACKAGE_PHONE_MANAGER),
                        PackageManager.MATCH_DEFAULT_ONLY);
                    if (resolveInfo != null && resolveInfo.activityInfo != null) {
                        mainIntent = new Intent();
                        mainIntent.setClassName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        Log.d("ShortcutHandler", "使用方法2启动手机管家: " + resolveInfo.activityInfo.name);
                        startActivity(mainIntent);
                        finish();
                        return;
                    }
                }
            } catch (Exception e) {
                Log.e("ShortcutHandler", "方法2失败: " + e.getMessage());
            }
            
            // 方法3: 使用 ACTION_MAIN
            try {
                Intent mainIntent = new Intent(Intent.ACTION_MAIN);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                mainIntent.setPackage(PACKAGE_PHONE_MANAGER);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Log.d("ShortcutHandler", "使用方法3启动手机管家");
                startActivity(mainIntent);
                finish();
                return;
            } catch (Exception e) {
                Log.e("ShortcutHandler", "方法3失败: " + e.getMessage());
            }
            
            Toast.makeText(this, "无法打开手机管家，请手动启动", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("ShortcutHandler", "打开手机管家异常", e);
            Toast.makeText(this, "无法打开手机管家: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finish();
    }
    
    private void openQinlinDoor() {
        try {
            PackageManager pm = getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(PACKAGE_QINLIN_DOOR);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(this, "未安装亲邻开门", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "无法打开亲邻开门: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}

