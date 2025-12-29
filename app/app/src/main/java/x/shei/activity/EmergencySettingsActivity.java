package x.shei.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import x.shei.R;

public class EmergencySettingsActivity extends BaseActivity {

    private EditText etEmergencyPhone;
    private EditText etWarningText;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_settings);

        sharedPreferences = getSharedPreferences("EmergencySettings", MODE_PRIVATE);
        
        initViews();
        loadSettings();
    }

    private void initViews() {
        etEmergencyPhone = findViewById(R.id.etEmergencyPhone);
        etWarningText = findViewById(R.id.etWarningText);
        Button btnSaveSettings = findViewById(R.id.btnSaveSettings);

        btnSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        String phone = sharedPreferences.getString("emergency_phone", "");
        String warningText = sharedPreferences.getString("warning_text", "着火了着火了");
        
        etEmergencyPhone.setText(phone);
        etWarningText.setText(warningText);
    }

    private void saveSettings() {
        String phone = etEmergencyPhone.getText().toString().trim();
        String warningText = etWarningText.getText().toString().trim();
        
        if (warningText.isEmpty()) {
            warningText = "着火了着火了";
        }
        
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("emergency_phone", phone);
        editor.putString("warning_text", warningText);
        editor.apply();
        
        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show();
        finish();
    }
}