package x.shei;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.alicloud.databox.opensdk.AliyunpanClient;
import x.shei.util.ImmersedUtil;

public class AuthLoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_login);
        ImmersedUtil.setImmersedMode(this, false);
    }

//    public void startOAuthQRCode(View v) {
//        AliyunpanClient aliyunpanClient = App.aliyunpanClient;
//        if (aliyunpanClient == null) {
//            return;
//        }
//        aliyunpanClient.oauthQRCode(task -> {
//                    Toast.makeText(this, "开始扫码授权", Toast.LENGTH_SHORT).show();
//                    String qrCodeUrl = task.getQRCodeUrl();
//                    Glide.with(this).load(qrCodeUrl).into((ImageView) v);
//
//                    task.addStateChange(status -> {
//                        switch (status) {
//                            case WAIT_LOGIN:
//                                Toast.makeText(this, "等待扫码", Toast.LENGTH_SHORT).show();
//                                break;
//                            case SCAN_SUCCESS:
//                                Toast.makeText(this, "扫码成功", Toast.LENGTH_SHORT).show();
//                                break;
//                            case LOGIN_SUCCESS:
//                                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
//                                break;
//                            case QRCODE_EXPIRED:
//                                Toast.makeText(this, "二维码过期", Toast.LENGTH_SHORT).show();
//                                break;
//                        }
//                    });
//                },
//                error -> Toast.makeText(this, "发起扫码授权失败", Toast.LENGTH_SHORT).show());
//    }

    public void  startOAuth(View v) {
        AliyunpanClient aliyunpanClient = App.aliyunpanClient;
        if (aliyunpanClient == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            aliyunpanClient.oauth(
                    success -> {
                        finish();
//                        Toast.makeText(this, "开始授权", Toast.LENGTH_SHORT).show();
                    },
                    // 处理失败的情况
                    error -> {
                        Toast.makeText(this, "发起授权失败", Toast.LENGTH_SHORT).show();
                    });
        }

    }
}
