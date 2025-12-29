package x.shei.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import x.shei.R;
import x.shei.activity.PaymentSuccessActivity;

public class ScanResultActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tvAmount;
    private StringBuilder amountBuilder = new StringBuilder();
    private static final int MAX_DECIMALS = 2;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);

        // 设置全屏和状态栏透明
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        // 初始化视图
        tvAmount = findViewById(R.id.tv_amount);

        // 设置数字键盘点击监听
        setNumberPadListeners();
    }

    private void setNumberPadListeners() {
        // 数字0-9
        for (int i = 0; i <= 9; i++) {
            String tag = String.valueOf(i);
            View button = findViewById(getResources().getIdentifier("btn_" + tag, "id", getPackageName()));
            if (button == null) {
                // 如果没有找到ID，尝试通过遍历查找带有tag的视图
                ViewGroup root = findViewById(android.R.id.content);
                button = findViewWithTag(root, tag);
            }
            if (button != null) {
                button.setOnClickListener(this);
            }
        }

        // 小数点
        View dotButton = findViewWithTag(findViewById(android.R.id.content), ".");
        if (dotButton != null) {
            dotButton.setOnClickListener(this);
        }

        // 删除按钮
        ImageButton deleteButton = findViewById(R.id.btn_delete);
        if (deleteButton != null) {
            deleteButton.setOnClickListener(v -> handleDelete());
        }

        // 确认支付按钮
        TextView confirmButton = findViewById(R.id.btn_confirm);
        if (confirmButton != null) {
            confirmButton.setOnClickListener(v -> handleConfirm());
        }
    }

    // 递归查找带有指定tag的视图
    private View findViewWithTag(View root, String tag) {
        if (tag.equals(root.getTag())) {
            return root;
        }

        if (root instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) root;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                View result = findViewWithTag(child, tag);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private void handleConfirm() {
        String amount = tvAmount.getText().toString();
        if (amount.isEmpty() || amount.equals("0")) {
            // 如果金额为空或0，不执行跳转
            return;
        }

        // 显示微信支付弹窗
        showWeChatPaymentDialog(amount);
    }

    private void showWeChatPaymentDialog(String amount) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setContentView(R.layout.dialog_wechat_payment);

        // 设置对话框窗口属性
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(params);
        }

        dialog.setCancelable(false);
        dialog.show();

        // 启动三个点的loading动画效果
        startDotLoadingAnimation(dialog);

        // 3秒后关闭弹窗并跳转
        handler.postDelayed(() -> {
            dialog.dismiss();
            // 跳转到支付成功页面
            Intent intent = new Intent(this, PaymentSuccessActivity.class);
            intent.putExtra("amount", amount);
            startActivity(intent);
        }, 3000);
    }

    private void startDotLoadingAnimation(Dialog dialog) {
        View dot1 = dialog.findViewById(R.id.dot1);
        View dot2 = dialog.findViewById(R.id.dot2);
        View dot3 = dialog.findViewById(R.id.dot3);

        if (dot1 == null || dot2 == null || dot3 == null) {
            return;
        }

        // 创建循环动画：左->中->右->左...
        animateDotCycle(dot1, dot2, dot3);
    }

    private void animateDotCycle(View dot1, View dot2, View dot3) {
        // 每个点的动画持续时间
        long duration = 400;

        // 使用圆形drawable资源
        android.graphics.drawable.Drawable darkDrawable = getResources().getDrawable(R.drawable.dot_indicator_dark);
        android.graphics.drawable.Drawable lightDrawable = getResources().getDrawable(R.drawable.dot_indicator_light);
        android.graphics.drawable.Drawable whiteDrawable = getResources().getDrawable(R.drawable.dot_indicator);

        // 创建循环任务
        Runnable[] cycleRunnable = new Runnable[1];
        cycleRunnable[0] = new Runnable() {
            @Override
            public void run() {
                // 状态1：左点白色，中点浅白，右点灰色
                setDotBackground(dot1, whiteDrawable);
                setDotBackground(dot2, lightDrawable);
                setDotBackground(dot3, darkDrawable);

                handler.postDelayed(() -> {
                    // 状态2：左点浅白，中点白色，右点灰色
                    setDotBackground(dot1, lightDrawable);
                    setDotBackground(dot2, whiteDrawable);
                    setDotBackground(dot3, darkDrawable);

                    handler.postDelayed(() -> {
                        // 状态3：左点灰色，中点浅白，右点白色
                        setDotBackground(dot1, darkDrawable);
                        setDotBackground(dot2, lightDrawable);
                        setDotBackground(dot3, whiteDrawable);

                        handler.postDelayed(() -> {
                            // 循环继续
                            handler.post(cycleRunnable[0]);
                        }, duration);
                    }, duration);
                }, duration);
            }
        };

        // 开始循环
        handler.post(cycleRunnable[0]);
    }

    private void setDotBackground(View dot, android.graphics.drawable.Drawable drawable) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            dot.setBackground(drawable);
        } else {
            dot.setBackgroundDrawable(drawable);
        }
    }

    @Override
    public void onClick(View v) {
        String input = (String) v.getTag();
        if (input != null) {
            handleInput(input);
        }
    }

    private void handleInput(String input) {
        // 处理小数点
        if (input.equals(".")) {
            if (!amountBuilder.toString().contains(".") && amountBuilder.length() > 0) {
                amountBuilder.append(input);
            }
            updateAmountDisplay();
            return;
        }

        // 处理数字输入
        String currentAmount = amountBuilder.toString();
        if (currentAmount.contains(".")) {
            // 已有小数点，检查小数位数
            String[] parts = currentAmount.split("\\.");
            if (parts.length > 1 && parts[1].length() >= MAX_DECIMALS) {
                return; // 已达到最大小数位数
            }
        }

        // 处理前导零
        if (currentAmount.equals("0") && !input.equals(".")) {
            amountBuilder.setLength(0);
        }

        amountBuilder.append(input);
        updateAmountDisplay();
    }

    private void handleDelete() {
        if (amountBuilder.length() > 0) {
            amountBuilder.setLength(amountBuilder.length() - 1);
            updateAmountDisplay();
        }
    }

    private void updateAmountDisplay() {
        String amount = amountBuilder.toString();
        if (amount.isEmpty()) {
            tvAmount.setText("0");
        } else {
            tvAmount.setText(amount);
        }
    }
}
