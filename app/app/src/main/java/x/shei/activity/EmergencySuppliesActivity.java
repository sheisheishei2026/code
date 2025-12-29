package x.shei.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import x.shei.R;
import x.shei.util.ImmersedUtil;

public class EmergencySuppliesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_supplies);
        ImmersedUtil.setImmersedMode(this, false);

        // 可以添加点击事件或其他交互功能
//        Toast.makeText(this, "应急用品指南 - 救生急救与战时储备", Toast.LENGTH_LONG).show();
    }
}
