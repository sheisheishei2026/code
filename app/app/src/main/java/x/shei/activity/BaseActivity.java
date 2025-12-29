package x.shei.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import androidx.fragment.app.FragmentActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import x.shei.util.EyeProtectionUtil;

public class BaseActivity extends FragmentActivity {

    private static final int REQUEST_OVERLAY_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setStatusBarTransparent();
        
        // 如果护眼模式已启用，自动应用（系统级）
        if (EyeProtectionUtil.isEnabled(this)) {
            if (EyeProtectionUtil.hasOverlayPermission(this)) {
                EyeProtectionUtil.enable(this);
            } else {
                // 如果没有权限，请求权限
                EyeProtectionUtil.requestOverlayPermission(this, REQUEST_OVERLAY_PERMISSION);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            // 权限授予后，如果护眼模式已启用，则启用它
            if (EyeProtectionUtil.hasOverlayPermission(this) && EyeProtectionUtil.isEnabled(this)) {
                EyeProtectionUtil.enable(this);
            }
        }
    }

    protected void setStatusBarTransparent() {
        // 让内容延伸到状态栏和导航栏
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // 设置状态栏颜色为透明
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        // 获取 WindowInsetsController
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            // 设置状态栏文字颜色为深色
            controller.setAppearanceLightStatusBars(true);

            // 显示状态栏
            controller.show(WindowInsetsCompat.Type.statusBars());
        }
    }

    protected int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
