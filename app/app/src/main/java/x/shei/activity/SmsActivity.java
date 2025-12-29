package x.shei.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import x.shei.R;
import x.shei.adapter.SmsAdapter;
import x.shei.db.SmsInfo;

public class SmsActivity extends BaseActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private RecyclerView recyclerView;
    private SmsAdapter smsAdapter;
    private List<SmsInfo> smsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        smsList = new ArrayList<>();
        smsAdapter = new SmsAdapter(smsList, this);
        recyclerView.setAdapter(smsAdapter);

        // 设置监听器
        smsAdapter.setOnSmsActionListener(new SmsAdapter.OnSmsActionListener() {
            @Override
            public void onViewAll(SmsInfo smsInfo) {
                showSmsDetailDialog(smsInfo);
            }

            @Override
            public void onDelete(SmsInfo smsInfo, int position) {
                showDeleteConfirmDialog(smsInfo, position);
            }
        });

        // 检查并请求权限
        if (checkSmsPermission()) {
            loadSms();
        } else {
            requestSmsPermission();
        }
    }

    private boolean checkSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestSmsPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_SMS},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadSms();
            } else {
                Toast.makeText(this, "需要短信读取权限才能查看短信", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadSms() {
        smsList.clear();
        ContentResolver contentResolver = getContentResolver();
        Uri uri = Uri.parse("content://sms/");
        String[] projection = {"_id", "address", "body", "date", "type"};

        Cursor cursor = null;
        try {
            cursor = contentResolver.query(uri, projection, null, null, "date DESC");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String id = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                    String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                    long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                    int type = cursor.getInt(cursor.getColumnIndexOrThrow("type"));

                    SmsInfo smsInfo = new SmsInfo(id, address, body, date, type);
                    smsList.add(smsInfo);
                }
                smsAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "读取短信失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void showSmsDetailDialog(SmsInfo smsInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sms_detail, null);
        
        TextView tvDetailAddress = dialogView.findViewById(R.id.tvDetailAddress);
        TextView tvDetailDate = dialogView.findViewById(R.id.tvDetailDate);
        TextView tvDetailBody = dialogView.findViewById(R.id.tvDetailBody);

        tvDetailAddress.setText("发送者: " + smsInfo.getAddress());
        String dateStr = DateFormat.format("yyyy-MM-dd HH:mm:ss", smsInfo.getDate()).toString();
        tvDetailDate.setText("时间: " + dateStr);
        tvDetailBody.setText(smsInfo.getBody());

        builder.setView(dialogView);
        builder.setTitle("短信详情");
        builder.setPositiveButton("关闭", null);
        builder.show();
    }

    private void showDeleteConfirmDialog(SmsInfo smsInfo, int position) {
        new AlertDialog.Builder(this)
                .setTitle("删除确认")
                .setMessage("确定要删除这条短信吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    deleteSms(smsInfo, position);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private boolean isDefaultSmsApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this);
            return defaultSmsPackage != null && defaultSmsPackage.equals(getPackageName());
        }
        return true; // Android 4.4以下版本不需要默认应用
    }

    private void requestDefaultSmsApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "无法打开设置页面", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteSms(SmsInfo smsInfo, int position) {
        // 检查是否是默认短信应用（Android 4.4+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !isDefaultSmsApp()) {
            new AlertDialog.Builder(this)
                    .setTitle("需要设置为默认短信应用")
                    .setMessage("从Android 4.4开始，只有默认短信应用才能删除短信。\n\n选择操作：")
                    .setPositiveButton("前往设置", (dialog, which) -> {
                        requestDefaultSmsApp();
                    })
                    .setNeutralButton("仅从列表移除", (dialog, which) -> {
                        smsAdapter.removeItem(position);
                        Toast.makeText(this, "已从列表中移除（实际短信未删除）", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return;
        }

        try {
            ContentResolver contentResolver = getContentResolver();
            String id = smsInfo.getId();
            int deleted = 0;

            // 方法1: 使用完整的URI
            try {
                Uri uri = Uri.parse("content://sms/" + id);
                deleted = contentResolver.delete(uri, null, null);
            } catch (Exception e1) {
                // 方法2: 使用_id字段和where条件
                try {
                    Uri uri = Uri.parse("content://sms/");
                    deleted = contentResolver.delete(uri, "_id=?", new String[]{id});
                } catch (Exception e2) {
                    // 方法3: 尝试使用不同的URI路径
                    try {
                        // 根据类型选择不同的URI
                        String uriPath = smsInfo.getType() == 1 ? "content://sms/inbox" : "content://sms/sent";
                        Uri uri = Uri.parse(uriPath);
                        deleted = contentResolver.delete(uri, "_id=?", new String[]{id});
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                }
            }

            if (deleted > 0) {
                smsAdapter.removeItem(position);
                Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "删除失败，请确保应用是默认短信应用", Toast.LENGTH_LONG).show();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "删除失败: 权限不足", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "删除失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

