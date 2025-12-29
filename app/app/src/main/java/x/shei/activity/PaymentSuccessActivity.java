package x.shei.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import x.shei.R;
import x.shei.util.ImmersedUtil;

public class PaymentSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);
        ImmersedUtil.setImmersedMode(this, false);

        // 获取传递过来的金额
        String amount = getIntent().getStringExtra("amount");
        TextView tvAmount = findViewById(R.id.tv_success_amount);

        // 格式化金额，确保有两位小数
        String formattedAmount = formatAmount(amount);
        tvAmount.setText("¥ " + formattedAmount);
        // 完成按钮点击事件
        Button btnDone = findViewById(R.id.btn_done);
        btnDone.setOnClickListener(v -> {
            // 关闭所有之前的Activity并返回到主页面
            finishAffinity();
        });
    }

    private String formatAmount(String amount) {
        if (amount == null || amount.isEmpty() || amount.equals("0")) {
            return "0.00";
        }

        try {
            // 解析金额
            double value = Double.parseDouble(amount);

            // 格式化为两位小数
            java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
            return df.format(value);
        } catch (NumberFormatException e) {
            // 如果解析失败，尝试处理已有格式
            if (amount.contains(".")) {
                String[] parts = amount.split("\\.");
                if (parts.length == 2) {
                    // 确保小数部分有两位
                    String decimalPart = parts[1];
                    if (decimalPart.length() == 1) {
                        decimalPart = decimalPart + "0";
                    } else if (decimalPart.length() > 2) {
                        decimalPart = decimalPart.substring(0, 2);
                    }
                    return parts[0] + "." + decimalPart;
                }
            }
            return amount + ".00";
        }

    }

    public void onCloseClick(View view) {
        finish();
    }
}
