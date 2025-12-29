package x.shei.activity;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import x.shei.R;
import x.shei.adapter.EntranceAdapter;
import x.shei.db.EntranceItem;
import x.shei.service.CombinedAccessibilityService;
import x.shei.util.EyeProtectionUtil;
import x.shei.util.ImmersedUtil;

public class APPActivity extends BaseActivity {

    private static final int REQUEST_PICK_IMAGE = 2001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance);
        ImmersedUtil.setImmersedMode(this, false);

        // 设置 App Shortcuts（需要 Android 7.1+ ）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            setupAppShortcuts();
        }

        // 设置护眼模式按钮
        Button btnEyeProtection = findViewById(R.id.btnEyeProtection);
        if (btnEyeProtection != null) {
            updateEyeProtectionButton(btnEyeProtection);
            btnEyeProtection.setOnClickListener(v -> {
                if (!EyeProtectionUtil.hasOverlayPermission(this)) {
                    // 请求悬浮窗权限
                    EyeProtectionUtil.requestOverlayPermission(this, 1001);
                    Toast.makeText(this, "需要悬浮窗权限才能使用系统级护眼模式", Toast.LENGTH_LONG).show();
                } else {
                    boolean success = EyeProtectionUtil.toggle(this);
                    updateEyeProtectionButton(btnEyeProtection);
                    String status = EyeProtectionUtil.isEnabled(this) ? "已开启" : "已关闭";
                    if (success) {
                        Toast.makeText(this, "系统护眼模式" + status, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "操作失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // 设置壁纸按钮
        Button btnSetWallpaper = findViewById(R.id.btnSetWallpaper);
        if (btnSetWallpaper != null) {
            btnSetWallpaper.setOnClickListener(v -> {
                pickImageForWallpaper();
            });
        }

        // 设置刷抖音极速按钮
        Button btnDouyinScroll = findViewById(R.id.btnDouyinScroll);
        if (btnDouyinScroll != null) {
            updateDouyinScrollButton(btnDouyinScroll);
            btnDouyinScroll.setOnClickListener(v -> {
                startDouyinAutoScroll();
                updateDouyinScrollButton(btnDouyinScroll);
            });
        }

        // 设置电池优化按钮
        Button btnBatteryOptimization = findViewById(R.id.btnBatteryOptimization);
        if (btnBatteryOptimization != null) {
            btnBatteryOptimization.setOnClickListener(v -> {
                x.shei.util.KeepAliveManager.requestBatteryOptimizationWhitelist(this);
            });
        }

        // 设置后台运行按钮
        Button btnBackgroundRunning = findViewById(R.id.btnBackgroundRunning);
        if (btnBackgroundRunning != null) {
            btnBackgroundRunning.setOnClickListener(v -> {
                x.shei.util.KeepAliveManager.requestBackgroundRunning(this);
            });
        }

        // 设置绑定音量键按钮
        Button btnBindVolumeKey = findViewById(R.id.btnBindVolumeKey);
        if (btnBindVolumeKey != null) {
            btnBindVolumeKey.setOnClickListener(v -> {
//                x.shei.util.KeepAliveManager.openAccessibilitySettings(this);
                // 启动音量键双击监听服务
                startVolumeKeyService();
            });
        }

        // 显示保活状态
        displayKeepAliveStatus();

        // 设置日历视图
        android.widget.CalendarView calendarView = findViewById(R.id.calendarView);
        if (calendarView != null) {
            // 确保从周一开始
            calendarView.setFirstDayOfWeek(Calendar.MONDAY);

            // 增大日历字体（延迟执行，确保视图已完全加载）
            // 多次尝试，因为CalendarView的子视图可能在后续才渲染
            Runnable setTextSizeRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        // 直接遍历所有子视图
                        setCalendarViewTextSize(calendarView, 28.0f);

                        // 使用反射访问CalendarView的内部ListView
                        try {
                            java.lang.reflect.Field listViewField = android.widget.CalendarView.class.getDeclaredField("mListView");
                            listViewField.setAccessible(true);
                            android.widget.ListView listView = (android.widget.ListView) listViewField.get(calendarView);
                            if (listView != null) {
                                for (int i = 0; i < listView.getChildCount(); i++) {
                                    View child = listView.getChildAt(i);
                                    setCalendarViewTextSize(child, 28.0f);
                                }
                            }
                        } catch (Exception e) {
                            Log.d("APPActivity", "反射访问ListView失败", e);
                        }
                    } catch (Exception e) {
                        Log.e("APPActivity", "设置日历字体大小失败", e);
                    }
                }
            };

            // 立即执行一次
            calendarView.post(setTextSizeRunnable);

            // 延迟300ms再执行一次
            calendarView.postDelayed(setTextSizeRunnable, 300);

            // 延迟600ms再执行一次，确保完全渲染
            calendarView.postDelayed(setTextSizeRunnable, 600);
            //
            //            calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            //                // 日期选择回调
            //                String selectedDate = String.format("%d年%d月%d日", year, month + 1, dayOfMonth);
            //                Toast.makeText(this, "选择了: " + selectedDate, Toast.LENGTH_SHORT).show();
            //            });
            //        }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Android 10 及以上需要请求额外权限
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            }, 2);
                }
            }

            {
                RecyclerView recyclerView = findViewById(R.id.entranceRecyclerView);
                recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
//            recyclerView.setPadding(recyclerView.getPaddingLeft(),
//                    getStatusBarHeight() + recyclerView.getPaddingTop(),
//                    recyclerView.getPaddingRight(),
//                    recyclerView.getPaddingBottom());

                List<EntranceItem> items = new ArrayList<>();
//            items.add(new EntranceItem("2048", DataList.get(6), GameActivity.class));

                items.add(new EntranceItem("X", DataList.get(0), MovieActivity.class));
                items.add(new EntranceItem("写真", DataList.get(1), MediaGalleryActivity.class));
                items.add(new EntranceItem("PDF", DataList.get(2), PdfListActivity.class));
                items.add(new EntranceItem("扫码", DataList.get(3), ScanActivity.class));
                items.add(new EntranceItem("景点", DataList.get(4), PoiActivity.class));
                items.add(new EntranceItem("H5", DataList.get(5), WebQQ.class));
                items.add(new EntranceItem("应用", DataList.get(6), AppsActivity.class));
                items.add(new EntranceItem("计算器", DataList.get(7), CalculatorActivity.class));
                items.add(new EntranceItem("紧急", DataList.get(8), EmergencyActivity.class));
                items.add(new EntranceItem("音效", DataList.get(9), SoundEffectsActivity.class));
                items.add(new EntranceItem("防空", DataList.get(10), AirRaidAlertActivity.class));
                items.add(new EntranceItem("应急", DataList.get(11), EmergencySuppliesActivity.class));

                EntranceAdapter adapter = new EntranceAdapter(this, items);
                recyclerView.setAdapter(adapter);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    // Android 10 及以上需要请求额外权限
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                }, 123);
                    }
                } else {
                    // Android 10 以下版本
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                123);
                    }
                }



            }
        }
    }

    /**
     * 更新刷抖音按钮的文本以反映当前状态
     */
    private void updateDouyinScrollButton(Button button) {
        boolean isAutoScrollEnabled = CombinedAccessibilityService.isAutoScrollEnabled(this);
        if (isAutoScrollEnabled) {
            button.setText("⏹️ 停刷极速");
        } else {
            button.setText("📱 刷极速");
        }
    }

    /**
     * 显示保活状态
     */
    private void displayKeepAliveStatus() {
        try {
            x.shei.util.KeepAliveStatus status = x.shei.util.KeepAliveManager.getKeepAliveStatus(this);

            StringBuilder statusText = new StringBuilder();
            statusText.append("保活状态:\n");
            statusText.append("• 电池优化: ").append(status.isBatteryOptimizationIgnored ? "✅ 已关闭" : "❌ 未关闭").append("\n");
            statusText.append("• 后台运行: ").append(status.isBackgroundRunningAllowed ? "✅ 已开启" : "❌ 未开启").append("\n");
            statusText.append("• 自启动: ").append(status.isAutoStartEnabled ? "✅ 已开启" : "❌ 未开启").append("\n");
            statusText.append("• 无障碍服务: ").append(status.isAccessibilityServiceEnabled ? "✅ 已开启" : "❌ 未开启").append("\n");

            android.widget.TextView statusTextView = findViewById(R.id.statusText);
            if (statusTextView != null) {
                statusTextView.setText(statusText.toString());
            }
        } catch (Exception e) {
            android.util.Log.e("APPActivity", "显示保活状态失败", e);

            android.widget.TextView statusTextView = findViewById(R.id.statusText);
            if (statusTextView != null) {
                statusTextView.setText("状态检查失败: " + e.getMessage());
            }
        }
    }

    public static List<String> DataList = new ArrayList<>();

    static {
        // 初始化DataList，按APPActivity中实际使用的顺序添加图标
        DataList.add(x.shei.util.FunctionIconMapper.getIconUrl("X"));
        DataList.add(x.shei.util.FunctionIconMapper.getIconUrl("写真"));
        DataList.add(x.shei.util.FunctionIconMapper.getIconUrl("PDF"));
        DataList.add(x.shei.util.FunctionIconMapper.getIconUrl("扫码"));
        DataList.add(x.shei.util.FunctionIconMapper.getIconUrl("景点"));
        DataList.add(x.shei.util.FunctionIconMapper.getIconUrl("H5"));
        DataList.add(x.shei.util.FunctionIconMapper.getIconUrl("应用"));
        DataList.add(x.shei.util.FunctionIconMapper.getIconUrl("计算器"));
        DataList.add(x.shei.util.FunctionIconMapper.getIconUrl("紧急"));
        DataList.add(x.shei.util.FunctionIconMapper.getIconUrl("音效"));
        DataList.add(x.shei.util.FunctionIconMapper.getIconUrl("防空"));
        DataList.add(x.shei.util.FunctionIconMapper.getIconUrl("应急"));
    }

    private void updateEyeProtectionButton(Button button) {
        if (EyeProtectionUtil.isEnabled(this)) {
            button.setText("✅ 护眼开");
            button.setBackgroundResource(R.drawable.eye_protection_button_enabled);
        } else {
            button.setText("👁️ 护眼关");
            button.setBackgroundResource(R.drawable.eye_protection_button_disabled);
        }
    }

    /**
     * 打开图片选择器或系统壁纸设置界面
     * Android 13+ 直接打开系统壁纸选择器，避免二次跳转
     */
    private void pickImageForWallpaper() {
        // Android 13+ 直接使用系统壁纸选择器
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d("APPActivity", "Android 13+ 直接打开系统壁纸选择器");
            openSystemWallpaperPicker();
        } else {
            // Android 13以下，先选择图片，然后设置
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
        }
    }

    /**
     * 打开系统壁纸选择器（Android 13+推荐方式）
     */
    private void openSystemWallpaperPicker() {
        try {
            // 方法1：使用ACTION_SET_WALLPAPER打开系统壁纸选择器
            Intent wallpaperIntent = new Intent(Intent.ACTION_SET_WALLPAPER);
            if (wallpaperIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(wallpaperIntent);
                return;
            }

            // 方法2：如果系统不支持，尝试打开系统设置中的壁纸选项
            Intent settingsIntent = new Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS);
            if (settingsIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(settingsIntent);
                Toast.makeText(this, "请在系统设置中选择壁纸", Toast.LENGTH_SHORT).show();
                return;
            }

            // 如果都失败，回退到选择图片的方式
            Log.w("APPActivity", "无法打开系统壁纸选择器，回退到图片选择方式");
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
        } catch (Exception e) {
            Log.e("APPActivity", "打开系统壁纸选择器失败", e);
            // 失败时回退到图片选择方式
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                setWallpaperFromUri(imageUri);
            }
        }
    }

    /**
     * 从URI设置壁纸 - 使用多种备用方案
     * 注意：Android 13+ 应该直接使用系统壁纸选择器，不会调用此方法
     */
    private void setWallpaperFromUri(Uri imageUri) {
        try {
            // Android 13 (API 33) 及以上版本，使用Intent方式
            // 注意：正常情况下Android 13+不会走到这里，因为已经直接打开了系统壁纸选择器
            // 但如果用户手动选择了图片（比如从其他应用分享），仍然需要处理
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Log.d("APPActivity", "Android 13+ 检测到，使用Intent方式设置壁纸");
                if (setWallpaperMethod3(imageUri)) {
                    // Intent方式会打开系统界面，用户确认后设置
                    return;
                }
            }

            // Android 13以下版本，先尝试直接设置
            // 方法1：尝试使用WallpaperManager.setBitmap()直接设置
            if (setWallpaperMethod1(imageUri)) {
                // 验证是否真的设置成功（Android 13上可能返回true但实际没生效）
                if (verifyWallpaperSet()) {
                    Toast.makeText(this, "壁纸设置成功", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Log.w("APPActivity", "方法1返回成功但验证失败，尝试Intent方式");
                }
            }

            // 方法2：尝试使用WallpaperManager.setStream()设置
            if (setWallpaperMethod2(imageUri)) {
                if (verifyWallpaperSet()) {
                    Toast.makeText(this, "壁纸设置成功", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Log.w("APPActivity", "方法2返回成功但验证失败，尝试Intent方式");
                }
            }

            // 方法3：使用Intent让系统处理（最可靠的方式）
            if (setWallpaperMethod3(imageUri)) {
                // 这个方法会打开系统壁纸设置界面，不需要显示成功提示
                return;
            }

            // 所有方法都失败
            Toast.makeText(this, "设置壁纸失败，请尝试手动设置", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e("APPActivity", "设置壁纸失败", e);
            // 最后尝试使用Intent方式
            if (!setWallpaperMethod3(imageUri)) {
                Toast.makeText(this, "设置壁纸失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 验证壁纸是否真的设置成功
     * 注意：这个方法可能在某些设备上不准确，仅供参考
     * 在Android 13上，即使setBitmap返回true，也可能实际没有生效
     */
    private boolean verifyWallpaperSet() {
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            // 尝试获取当前壁纸ID，如果能获取到说明可能设置成功
            // 注意：这个方法在某些设备上可能不准确，所以不能完全依赖
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Android 7.0+ 可以获取壁纸ID
                // FLAG_SYSTEM = 1, FLAG_LOCK = 2
                int wallpaperId = wallpaperManager.getWallpaperId(1); // FLAG_SYSTEM
                Log.d("APPActivity", "当前壁纸ID: " + wallpaperId);
                return wallpaperId > 0;
            }
            // Android 7.0以下，无法准确验证，返回true
            return true;
        } catch (Exception e) {
            Log.e("APPActivity", "验证壁纸设置失败", e);
            // 验证失败时返回true，避免误判（因为验证方法本身可能不准确）
            return true;
        }
    }

    /**
     * 方法1：使用WallpaperManager.setBitmap()直接设置
     */
    private boolean setWallpaperMethod1(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e("APPActivity", "方法1: 无法打开输入流");
                return false;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) {
                Log.e("APPActivity", "方法1: 无法解码图片");
                return false;
            }

            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);

            // Android 7.1+ 可以使用flags参数
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                wallpaperManager.setBitmap(bitmap, null, true);
                Log.d("APPActivity", "方法1: 使用setBitmap(bitmap, null, true)");
            } else {
                wallpaperManager.setBitmap(bitmap);
                Log.d("APPActivity", "方法1: 使用setBitmap(bitmap)");
            }

            // 注意：即使没有抛出异常，在某些设备（特别是Android 13）上可能实际没有生效
            Log.d("APPActivity", "方法1: setBitmap调用完成，无异常");
            return true;
        } catch (SecurityException e) {
            Log.e("APPActivity", "方法1设置壁纸失败: 权限不足", e);
            return false;
        } catch (IOException e) {
            Log.e("APPActivity", "方法1设置壁纸失败: IO异常", e);
            return false;
        } catch (Exception e) {
            Log.e("APPActivity", "方法1设置壁纸失败", e);
            return false;
        }
    }

    /**
     * 方法2：使用WallpaperManager.setStream()通过流设置
     */
    private boolean setWallpaperMethod2(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e("APPActivity", "方法2: 无法打开输入流");
                return false;
            }

            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            wallpaperManager.setStream(inputStream);
            inputStream.close();

            Log.d("APPActivity", "方法2: setStream调用完成，无异常");
            return true;
        } catch (SecurityException e) {
            Log.e("APPActivity", "方法2设置壁纸失败: 权限不足", e);
            return false;
        } catch (IOException e) {
            Log.e("APPActivity", "方法2设置壁纸失败: IO异常", e);
            return false;
        } catch (Exception e) {
            Log.e("APPActivity", "方法2设置壁纸失败", e);
            return false;
        }
    }

    /**
     * 方法3：使用Intent让系统处理壁纸设置（最可靠的方式，特别是Android 13+）
     * 这会打开系统壁纸设置界面，让用户确认设置
     */
    private boolean setWallpaperMethod3(Uri imageUri) {
        try {
            // 方法3a：使用ACTION_ATTACH_DATA（推荐，Android标准方式）
            // 这个Intent会打开系统壁纸预览界面，用户确认后设置
            Intent attachIntent = new Intent(Intent.ACTION_ATTACH_DATA);
            attachIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            attachIntent.setDataAndType(imageUri, "image/*");
            attachIntent.putExtra("mimeType", "image/*");

            // 对于Android 13+，添加额外的标志
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                attachIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            // 检查是否有应用可以处理这个Intent
            if (attachIntent.resolveActivity(getPackageManager()) != null) {
                Log.d("APPActivity", "方法3a: 使用ACTION_ATTACH_DATA打开系统壁纸设置界面");
                startActivity(Intent.createChooser(attachIntent, "选择应用设置壁纸"));
                Toast.makeText(this, "请在系统界面中确认设置壁纸", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                Log.w("APPActivity", "方法3a: 没有应用可以处理ACTION_ATTACH_DATA");
            }

            // 方法3b：备用方案 - 使用ACTION_SET_WALLPAPER（打开系统壁纸选择器）
            // 注意：这个方法需要用户手动选择图片，但更兼容
            Intent wallpaperIntent = new Intent(Intent.ACTION_SET_WALLPAPER);
            if (wallpaperIntent.resolveActivity(getPackageManager()) != null) {
                Log.d("APPActivity", "方法3b: 使用ACTION_SET_WALLPAPER打开系统壁纸选择器");
                startActivity(wallpaperIntent);
                Toast.makeText(this, "请在系统界面中选择图片", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                Log.w("APPActivity", "方法3b: 没有应用可以处理ACTION_SET_WALLPAPER");
            }

            return false;
        } catch (SecurityException e) {
            Log.e("APPActivity", "方法3设置壁纸失败: 权限不足", e);
            Toast.makeText(this, "权限不足，无法设置壁纸", Toast.LENGTH_SHORT).show();
            return false;
        } catch (Exception e) {
            Log.e("APPActivity", "方法3设置壁纸失败", e);
            return false;
        }
    }

    /**
     * 设置 App Shortcuts（长按图标快捷功能）
     * 需要 Android 7.1 (API 25) 及以上版本
     */
    private void setupAppShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            try {
                android.content.pm.ShortcutManager shortcutManager = getSystemService(android.content.pm.ShortcutManager.class);
                if (shortcutManager == null) {
                    return;
                }

                List<android.content.pm.ShortcutInfo> shortcuts = new ArrayList<>();

                // 快捷方式1: 打开设置
                Intent settingsIntent = new Intent(this, ShortcutHandlerActivity.class);
                settingsIntent.setAction(ShortcutHandlerActivity.ACTION_OPEN_SETTINGS);
                settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                android.content.pm.ShortcutInfo settingsShortcut = new android.content.pm.ShortcutInfo.Builder(this, "shortcut_settings")
                        .setShortLabel("设置")
                        .setLongLabel("打开系统设置")
                        .setIcon(android.graphics.drawable.Icon.createWithResource(this, android.R.drawable.ic_menu_preferences))
                        .setIntent(settingsIntent)
                        .build();
                shortcuts.add(settingsShortcut);

                // 快捷方式2: 打开手机管家
//                Intent phoneManagerIntent = new Intent(this, ShortcutHandlerActivity.class);
//                phoneManagerIntent.setAction(ShortcutHandlerActivity.ACTION_OPEN_PHONE_MANAGER);
//                phoneManagerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

//                android.content.pm.ShortcutInfo phoneManagerShortcut = new android.content.pm.ShortcutInfo.Builder(this, "shortcut_phone_manager")
//                        .setShortLabel("手机管家")
//                        .setLongLabel("打开手机管家")
//                        .setIcon(android.graphics.drawable.Icon.createWithResource(this, android.R.drawable.ic_menu_manage))
//                        .setIntent(phoneManagerIntent)
//                        .build();
//                shortcuts.add(phoneManagerShortcut);

                // 快捷方式3: 打开亲邻开门
                Intent qinlinIntent = new Intent(this, ShortcutHandlerActivity.class);
                qinlinIntent.setAction(ShortcutHandlerActivity.ACTION_OPEN_QINLIN_DOOR);
                qinlinIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                android.content.pm.ShortcutInfo qinlinShortcut = new android.content.pm.ShortcutInfo.Builder(this, "shortcut_qinlin_door")
                        .setShortLabel("亲邻开门")
                        .setLongLabel("打开亲邻开门")
                        .setIcon(android.graphics.drawable.Icon.createWithResource(this, android.R.drawable.ic_menu_view))
                        .setIntent(qinlinIntent)
                        .build();
                shortcuts.add(qinlinShortcut);

                // 设置快捷方式
                shortcutManager.setDynamicShortcuts(shortcuts);
            } catch (Exception e) {
                Log.e("APPActivity", "设置 App Shortcuts 失败", e);
            }
        }
    }

    /**
     * 开始刷抖音极速版自动滑动
     */
    private void startDouyinAutoScroll() {
        // 抖音极速版包名
        String douyinPackage = "com.ss.android.ugc.aweme.lite";

        // 先检查无障碍服务是否已启用
        if (!isAccessibilityServiceEnabled()) {
            // 引导用户去设置页面开启无障碍权限
            Toast.makeText(this, "需要开启无障碍权限才能使用自动滑动功能", Toast.LENGTH_LONG).show();
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            } catch (Exception e) {
                Log.e("APPActivity", "无法打开无障碍设置", e);
                Toast.makeText(this, "请手动前往设置->无障碍->开启本应用的无障碍服务", Toast.LENGTH_LONG).show();
            }
            return;
        }

        // 检查抖音是否已安装
        try {
            getPackageManager().getPackageInfo(douyinPackage, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "未安装抖音极速版，请先安装", Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查当前自动滑动状态并切换
        boolean isCurrentlyEnabled = CombinedAccessibilityService.isAutoScrollEnabled(this);
        if (isCurrentlyEnabled) {
            // 如果当前是开启状态，则关闭
            CombinedAccessibilityService.setAutoScrollEnabled(this, false);
            Toast.makeText(this, "已关闭刷抖音模式", Toast.LENGTH_SHORT).show();
        } else {
            // 如果当前是关闭状态，则开启
            CombinedAccessibilityService.setAutoScrollEnabled(this, true);
            // 打开抖音极速版
            try {
                Intent intent = getPackageManager().getLaunchIntentForPackage(douyinPackage);
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    Toast.makeText(this, "已启动刷抖音模式，每10秒自动滑动", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "无法打开抖音极速版", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("APPActivity", "打开抖音失败", e);
                Toast.makeText(this, "打开抖音失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 检查无障碍服务是否已启用
     */
    private boolean isAccessibilityServiceEnabled() {
        String serviceName = getPackageName() + "/" + CombinedAccessibilityService.class.getName();
        try {
            int accessibilityEnabled = android.provider.Settings.Secure.getInt(
                    getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            if (accessibilityEnabled == 1) {
                String settingValue = android.provider.Settings.Secure.getString(
                        getContentResolver(),
                        android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                if (settingValue != null) {
                    return settingValue.toLowerCase().contains(serviceName.toLowerCase());
                }
            }
        } catch (Exception e) {
            Log.e("APPActivity", "检查无障碍服务状态失败", e);
        }
        return false;
    }

    private void startVolumeKeyService() {
        // 检查无障碍服务是否已启用
        if (!isVolumeKeyAccessibilityServiceEnabled()) {
            // 引导用户去设置页面开启无障碍权限
            Toast.makeText(this, "需要开启无障碍权限才能使用双击音量键功能", Toast.LENGTH_LONG).show();
            try {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            } catch (Exception e) {
                Log.e("APPActivity", "无法打开无障碍设置", e);
                Toast.makeText(this, "请手动前往设置->无障碍->开启本应用的音量键监听服务", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "无障碍服务已开启，正在启动保活机制", Toast.LENGTH_SHORT).show();
            
            // 启动保活机制
            startKeepAliveServices();
        }
    }
    
    /**
     * 启动保活服务
     */
    private void startKeepAliveServices() {
        try {
            // 启动前台服务以提高存活率
            Intent keepAliveIntent = new Intent(this, x.shei.service.KeepAliveService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(keepAliveIntent);
            } else {
                startService(keepAliveIntent);
            }
            
            Log.d("APPActivity", "保活服务已启动");
        } catch (Exception e) {
            Log.e("APPActivity", "启动保活服务失败", e);
        }
    }

    private boolean isVolumeKeyAccessibilityServiceEnabled() {
        String serviceName = getPackageName() + "/" + CombinedAccessibilityService.class.getName();
        try {
            int accessibilityEnabled = Settings.Secure.getInt(
                    getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
            if (accessibilityEnabled == 1) {
                String settingValue = Settings.Secure.getString(
                        getContentResolver(),
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                if (settingValue != null) {
                    return settingValue.toLowerCase().contains(serviceName.toLowerCase());
                }
            }
        } catch (Exception e) {
            Log.e("APPActivity", "检查音量键监听服务状态失败", e);
        }
        return false;
    }

    // 递归设置CalendarView中所有TextView的字体大小
    private void setCalendarViewTextSize(View view, float textSize) {
        if (view == null) {
            return;
        }

        if (view instanceof android.widget.TextView) {
            android.widget.TextView textView = (android.widget.TextView) view;
            // 检查是否是日期数字（避免修改标题等其他文本）
            String text = textView.getText().toString();
            if (!text.isEmpty() && (text.matches("\\d+") || text.matches("[一二三四五六日]"))) {
                textView.setTextSize(textSize);
                Log.d("APPActivity", "设置字体大小: " + text + " -> " + textSize);
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                setCalendarViewTextSize(group.getChildAt(i), textSize);
            }
        }
    }


}

