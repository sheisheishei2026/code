package x.shei.activity;

import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.Settings;
import android.view.Surface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import x.shei.R;
import x.shei.db.Bean;
import x.shei.db.FavDao;
import x.shei.db.FavDatabase;
import x.shei.db.FavEntity;

public class PlayActivity2 extends Activity {

    private PlayerView playerView;
    private ExoPlayer player;
    private float touchStartX;
    private float touchStartY;
    private static final int SWIPE_THRESHOLD = 50;  // 最小滑动阈值
    private boolean isSliding = false;  // 是否正在滑动
    private boolean isVerticalSliding = false;  // 是否正在垂直滑动

    // 双击检测
    private static final int DOUBLE_TAP_TIMEOUT = 300;  // 双击间隔时间（毫秒）
    private long lastTapTime = 0;
    private int tapCount = 0;
    private ClipboardManager clipboard;
    private String m3u8;
    private int type;
    private String a2;
    private String src;
    private Bean bean;

    // 亮度和音量控制
    private AudioManager audioManager;
    private float currentBrightness = -1;  // 当前亮度值
    private int currentVolume = -1;  // 当前音量值
    private float lastBrightnessY = 0;  // 上次亮度调节Y坐标
    private float lastVolumeY = 0;  // 上次音量调节Y坐标

    // 信息展示区域
    private ScrollView infoContainer;
    private TextView tvTitle;
    private TextView tvCountry;
    private TextView tvYear;
    private TextView tvIntro;

    private boolean wasPlaying = false;  // 添加标记记录播放状态

    private FavDatabase favDb;
    private FavDao favDao;

    // 录制相关
    private boolean isRecording = false;
    private MediaCodec videoEncoder;
    private MediaCodec audioEncoder;
    private MediaMuxer mediaMuxer;
    private Surface recordingSurface;
    private int videoTrackIndex = -1;
    private int audioTrackIndex = -1;
    private File recordingFile;
    private Handler recordingHandler;
    private Thread recordingThread;
    private Thread audioRecordingThread;
    private long recordingStartTime = 0;
    private Handler frameCaptureHandler;
    private AudioRecord audioRecord;
    private int audioSampleRate = 44100;
    private int audioChannelCount = 2;
    private int audioBitrate = 128000; // 128 kbps

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video);

        // 初始化视图和设置点击监听
        initViews();

        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        m3u8 = getIntent().getStringExtra("m3u8");
        a2 = getIntent().getStringExtra("a2");
        src = getIntent().getStringExtra("src");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bean = getIntent().getSerializableExtra("bean",Bean.class);
        }
        type = getIntent().getIntExtra("type",-1);

        // 初始化信息展示区域
        initInfoViews();

        if (m3u8 != null) {
            Log.e("asd",m3u8);
            playerView = findViewById(R.id.player_view);

            // 初始化ExoPlayer
            player = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(player);

            // 根据当前屏幕方向设置布局（在playerView初始化后调用）
            // 使用post确保控制栏View已经创建
            playerView.post(() -> {
                updateLayoutForOrientation(getResources().getConfiguration().orientation);
            });

            // 设置播放源
            Uri uri = Uri.parse(m3u8);
            MediaItem mediaItem = MediaItem.fromUri(uri);
            player.setMediaItem(mediaItem);

            // 准备播放器
            player.prepare();
            player.play();

            playerView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int viewWidth = v.getWidth();
                    float touchX = event.getX();
                    boolean isLeftSide = touchX < viewWidth / 2;  // 左侧区域

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            touchStartX = event.getX();
                            touchStartY = event.getY();
                            isSliding = false;
                            isVerticalSliding = false;

                            // 初始化亮度和音量值
                            if (isLeftSide) {
                                // 左侧：准备调节亮度
                                if (currentBrightness < 0) {
                                    // 首次触摸，获取当前亮度
                                    WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
                                    currentBrightness = layoutParams.screenBrightness;
                                    if (currentBrightness < 0) {
                                        // 如果使用系统亮度，尝试获取系统亮度值
                                        try {
                                            currentBrightness = Settings.System.getInt(getContentResolver(),
                                                Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
                                        } catch (Settings.SettingNotFoundException e) {
                                            currentBrightness = 0.5f;  // 默认值
                                        }
                                    }
                                }
                                lastBrightnessY = touchStartY;
                            } else {
                                // 右侧：准备调节音量
                                if (currentVolume < 0) {
                                    // 首次触摸，获取当前音量
                                    currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                }
                                lastVolumeY = touchStartY;
                            }
                            return false;

                        case MotionEvent.ACTION_MOVE:
                            float deltaX = event.getX() - touchStartX;
                            float deltaY = event.getY() - touchStartY;

                            // 判断是否为水平滑动（快进/快退）
                            if (Math.abs(deltaX) > Math.abs(deltaY) &&
                                Math.abs(deltaX) > SWIPE_THRESHOLD) {
                                isSliding = true;
                                isVerticalSliding = false;
                                // 滑动时重置双击计数
                                tapCount = 0;
                                return true;  // 开始滑动后拦截事件
                            }

                            // 判断是否为垂直滑动（亮度/音量）
                            if (Math.abs(deltaY) > Math.abs(deltaX) &&
                                Math.abs(deltaY) > SWIPE_THRESHOLD) {
                                isVerticalSliding = true;
                                isSliding = false;
                                tapCount = 0;

                                if (isLeftSide) {
                                    // 左侧：调节亮度
                                    float incrementalDeltaY = event.getY() - lastBrightnessY;
                                    adjustBrightness(incrementalDeltaY);
                                    lastBrightnessY = event.getY();
                                } else {
                                    // 右侧：调节音量
                                    float incrementalDeltaY = event.getY() - lastVolumeY;
                                    adjustVolume(incrementalDeltaY);
                                    lastVolumeY = event.getY();
                                }
                                return true;
                            }
                            return false;  // 不是滑动则传递给播放器

                        case MotionEvent.ACTION_UP:
                            if (isSliding) {
                                float deltaX2 = event.getX() - touchStartX;
                                // 根据滑动距离计算步长
                                long seekTime = calculateSeekTime(Math.abs(deltaX2));

                                // 获取当前播放位置
                                long currentPosition = player.getCurrentPosition();

                                if (deltaX2 > 0) {
                                    // 右滑快进
                                    player.seekTo(Math.min(currentPosition + seekTime,
                                        player.getDuration()));
                                    showSeekToast("快进 " + formatTime(seekTime));
                                } else {
                                    // 左滑快退
                                    player.seekTo(Math.max(currentPosition - seekTime, 0));
                                    showSeekToast("快退 " + formatTime(seekTime));
                                }
                                isSliding = false;
                                tapCount = 0;  // 滑动后重置双击计数
                                return true;
                            } else if (isVerticalSliding) {
                                // 垂直滑动结束，重置状态
                                isVerticalSliding = false;
                                // 重置当前值，下次滑动时重新获取
                                currentBrightness = -1;
                                currentVolume = -1;
                                return true;
                            } else {
                                // 检测双击
                                long currentTime = System.currentTimeMillis();
                                float deltaX2 = Math.abs(event.getX() - touchStartX);
                                float deltaY2 = Math.abs(event.getY() - touchStartY);

                                // 判断点击距离是否在阈值内（避免移动过大误判为点击）
                                if (deltaX2 < 50 && deltaY2 < 50) {
                                    if (tapCount == 0 || (currentTime - lastTapTime) < DOUBLE_TAP_TIMEOUT) {
                                        tapCount++;
                                        lastTapTime = currentTime;

                                        // 使用Handler延迟检测双击
                                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                            if (tapCount >= 2) {
                                                // 双击：切换播放/暂停
                                                togglePlayPause();
                                                tapCount = 0;
                                            } else if (tapCount == 1) {
                                                // 单次点击，不处理（交给播放器控件处理）
                                                tapCount = 0;
                                            }
                                        }, DOUBLE_TAP_TIMEOUT);
                                    } else {
                                        tapCount = 1;
                                        lastTapTime = currentTime;
                                    }
                                }
                            }
                            return false;
                    }
                    return false;
                }
            });
        }

        // 初始化数据库
        favDb = FavDatabase.getInstance(this);
        favDao = favDb.favDao();
    }

    private void initViews() {
        // 找到所有需要设置监听的按钮
        View download = findViewById(R.id.exo_download);
        View favoriteButton = findViewById(R.id.exo_favorite);
        // View downloadButton = findViewById(R.id.download_button);
        View copyButton = findViewById(R.id.exo_copy);
        View screenshotButton = findViewById(R.id.exo_screenshot);
        View recordButton = findViewById(R.id.exo_record);
        // View fastForwardButton = findViewById(R.id.fast_forward_button);

        // 设置点击监听器
        favoriteButton.setOnClickListener(v -> favorite());
        download.setVisibility(View.GONE);
//        download.setOnClickListener(v -> fullscreen());
        copyButton.setOnClickListener(v -> copy());
        screenshotButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                takeScreenshot();
            }
        });
        recordButton.setOnClickListener(v -> toggleRecording());
        // fastForwardButton.setOnClickListener(v -> kuaijin());
    }

    private void initInfoViews() {
        // 初始化信息展示视图
        infoContainer = findViewById(R.id.info_container);
        infoContainer.setVisibility(VISIBLE);
        tvTitle = findViewById(R.id.tv_title);
        tvCountry = findViewById(R.id.tv_country);
        tvYear = findViewById(R.id.tv_year);
        tvIntro = findViewById(R.id.tv_intro);

        // 填充数据
        if (bean != null) {
            if (bean.getTitle() != null) {
                tvTitle.setText(bean.getTitle());
            }

            if (bean.getCountry() != null && !bean.getCountry().isEmpty()) {
                tvCountry.setText("国家/地区: " + bean.getCountry());
            } else {
                tvCountry.setVisibility(View.GONE);
            }

            if (bean.getUpdateTime() != null && !bean.getUpdateTime().isEmpty()) {
                // 从更新时间中提取年份（假设格式类似 "2024-01-01" 或 "2024"）
                String year = extractYear(bean.getUpdateTime());
                tvYear.setText("年份: " + year);
            } else {
                tvYear.setVisibility(View.GONE);
            }

            if (bean.getIntro() != null && !bean.getIntro().isEmpty()) {
                tvIntro.setText("简介: " + bean.getIntro());
            } else {
                tvIntro.setVisibility(View.GONE);
            }
        }
    }

    private String extractYear(String updateTime) {
        if (updateTime == null || updateTime.isEmpty()) {
            return "";
        }
        // 尝试提取年份（格式可能是 "2024-01-01" 或 "2024"）
        if (updateTime.length() >= 4) {
            try {
                String yearPart = updateTime.substring(0, 4);
                Integer.parseInt(yearPart); // 验证是否为数字
                return yearPart;
            } catch (NumberFormatException e) {
                return updateTime; // 如果提取失败，返回原始值
            }
        }
        return updateTime;
    }

    private void updateLayoutForOrientation(int orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 横屏：隐藏信息区域，移除播放器底部边距（全屏显示）
            if (infoContainer != null) {
                infoContainer.setVisibility(View.GONE);
            }
            if (playerView != null) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) playerView.getLayoutParams();
                params.bottomMargin = 0;
                playerView.setLayoutParams(params);
                // 横屏时控制栏有底部边距（60dp），让进度条往上移
                updateControlViewMargin(true);
            }
        } else {
            // 竖屏：显示信息区域，添加播放器底部边距（往上移）
            if (infoContainer != null) {
                infoContainer.setVisibility(VISIBLE);
            }
            if (playerView != null) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) playerView.getLayoutParams();
                params.bottomMargin = (int) (60 * getResources().getDisplayMetrics().density); // 60dp转px
                playerView.setLayoutParams(params);
                // 竖屏时移除控制栏底部边距，让进度条不遮挡视频
                updateControlViewMargin(false);
            }
        }
    }

    // 更新控制栏的底部边距
    private void updateControlViewMargin(boolean isLandscape) {
        if (playerView == null) return;

        // 查找控制栏容器View（通过我们添加的id）
        View controlView = playerView.findViewById(R.id.exo_control_view_container);

        if (controlView != null) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) controlView.getLayoutParams();
            if (params != null) {
                if (isLandscape) {
                    // 横屏：控制栏有底部边距（60dp），让进度条往上移
                    params.bottomMargin = (int) (60 * getResources().getDisplayMetrics().density);
                } else {
                    // 竖屏：移除控制栏底部边距，让进度条不遮挡视频
                    params.bottomMargin = 0;
                }
                controlView.setLayoutParams(params);
            }
        }
    }

    // 根据滑动距离计算步长
    private long calculateSeekTime(float distance) {
        // 将滑动距离映射到5级别：20秒、1分钟、2分钟、3分钟、5分钟
        if (distance < 200) {
            return 20_000;  // 20秒
        } else if (distance < 500) {
            return 60_000;  // 1分钟
        } else if (distance < 700) {
            return 120_000;  // 2分钟
        } else if (distance < 1000) {
            return 180_000;  // 3分钟
        } else {
            return 300_000;  // 5分钟
        }
    }

    // 格式化时间显示
    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        if (seconds < 60) {
            return seconds + "秒";
        } else {
            long minutes = seconds / 60;
            seconds = seconds % 60;
            if (seconds == 0) {
                return minutes + "分钟";
            } else {
                return minutes + "分" + seconds + "秒";
            }
        }
    }

    private void showSeekToast(String message) {
        Toast makeText = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        // 显示在屏幕中间
        makeText.setGravity(Gravity.CENTER, 0, 0);
        makeText.show();
    }

    private void togglePlayPause() {
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();
            } else {
                player.play();
            }
        }
    }

    // 调节亮度
    private void adjustBrightness(float incrementalDeltaY) {
        try {
            int viewHeight = playerView.getHeight();
            if (viewHeight <= 0 || currentBrightness < 0) return;

            // 计算滑动距离占视图高度的比例（整个屏幕高度对应100%变化）
            float deltaPercent = -incrementalDeltaY / viewHeight;  // 负号：上滑增加亮度

            // 计算新的亮度值（0.0 - 1.0）
            float newBrightness = currentBrightness + deltaPercent;
            newBrightness = Math.max(0.0f, Math.min(1.0f, newBrightness));  // 限制在0-1之间

            // 更新当前亮度值
            currentBrightness = newBrightness;

            // 设置系统亮度
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.screenBrightness = newBrightness;
            getWindow().setAttributes(layoutParams);

            // 不显示亮度提示（用户要求移除toast）
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 调节音量
    private void adjustVolume(float incrementalDeltaY) {
        try {
            int viewHeight = playerView.getHeight();
            if (viewHeight <= 0 || currentVolume < 0) return;

            // 获取最大音量
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            // 计算滑动距离占视图高度的比例（整个屏幕高度对应100%变化）
            float deltaPercent = -incrementalDeltaY / viewHeight;  // 负号：上滑增加音量

            // 计算新的音量值
            int volumeChange = (int) (maxVolume * deltaPercent);
            int newVolume = currentVolume + volumeChange;
            newVolume = Math.max(0, Math.min(maxVolume, newVolume));  // 限制在0-maxVolume之间

            // 更新当前音量值
            currentVolume = newVolume;

            // 设置音量
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);

            // 不显示音量提示（用户要求移除toast）
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 显示亮度/音量提示
    private Toast brightnessVolumeToast;
    private void showBrightnessVolumeToast(String message, boolean isBrightness) {
        // 取消之前的Toast
        if (brightnessVolumeToast != null) {
            brightnessVolumeToast.cancel();
        }

        brightnessVolumeToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        brightnessVolumeToast.setGravity(Gravity.CENTER, 0, 0);
        brightnessVolumeToast.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 检查屏幕方向
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 横屏时全屏播放，隐藏信息区域
            if (getActionBar() != null) {
                getActionBar().hide();
            }
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            updateLayoutForOrientation(Configuration.ORIENTATION_LANDSCAPE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 竖屏时退出全屏，显示信息区域
            if (getActionBar() != null) {
                getActionBar().show();
            }
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            updateLayoutForOrientation(Configuration.ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (playerView != null) {
            playerView.onResume();
        }
        // 如果之前是在播放状态，恢复播放
        if (player != null && wasPlaying) {
            player.play();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (playerView != null) {
            playerView.onPause();
        }
        // 记录当前是否在播放
        wasPlaying = player != null && player.isPlaying();
        // 暂停播放
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 停止录制
        if (isRecording) {
            stopRecording();
        }
        // 释放播放器资源
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void favorite() {
        // 在后台线程中执行数据库操作
        new Thread(() -> {
            try {
                FavEntity fav = new FavEntity(src, m3u8, a2, type);
                long result = favDao.insert(fav);

                // 在主线程中显示结果
                runOnUiThread(() -> {
                    if (result != -1) {
                        Toast.makeText(this, "收藏成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "已经收藏过了", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "收藏失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void fullscreen() {
        Intent intent  =new Intent(this, PlayActivity2.class);
        startActivity(intent);
    }

    private void copy() {
        ClipData clip = ClipData.newPlainText("url", m3u8);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void takeScreenshot() {
        try {
            View videoSurfaceView = playerView.getVideoSurfaceView();
            if (!(videoSurfaceView instanceof SurfaceView)) {
                Toast.makeText(this, "不支持的视图类型", Toast.LENGTH_SHORT).show();
                return;
            }

            // 获取视频的原始尺寸
            Format format = player.getVideoFormat();
            if (format == null) {
                Toast.makeText(this, "无法获取视频信息", Toast.LENGTH_SHORT).show();
                return;
            }

            // 计算实际显示尺寸，保持宽高比
            int videoWidth = format.width;
            int videoHeight = format.height;
            float videoRatio = (float) videoWidth / videoHeight;

            int surfaceWidth = videoSurfaceView.getWidth();
            int surfaceHeight = videoSurfaceView.getHeight();
            float surfaceRatio = (float) surfaceWidth / surfaceHeight;

            int targetWidth, targetHeight;
            if (surfaceRatio > videoRatio) {
                // 以高度为基准
                targetHeight = surfaceHeight;
                targetWidth = (int) (surfaceHeight * videoRatio);
            } else {
                // 以宽度为基准
                targetWidth = surfaceWidth;
                targetHeight = (int) (surfaceWidth / videoRatio);
            }

            // 创建Bitmap
            Bitmap bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);

            // 使用PixelCopy API
            PixelCopy.request(
                    (SurfaceView) videoSurfaceView,
                    bitmap,
                    copyResult -> {
                        if (copyResult == PixelCopy.SUCCESS) {
                            saveScreenshot(bitmap);
                        } else {
                            Toast.makeText(this, "截图失败", Toast.LENGTH_SHORT).show();
                        }
                    },
                    new Handler(Looper.getMainLooper())
            );
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "截图失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveScreenshot(Bitmap bitmap) {
        String fileName = "X" + System.currentTimeMillis() + ".jpg";
        File path = new File(Environment.getExternalStorageDirectory(), "Pictures/x");
        if (!path.exists()) {
            path.mkdirs();
        }

        File file = new File(path, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();

            // 通知媒体库更新
            MediaScannerConnection.scanFile(
                this,
                new String[]{file.getAbsolutePath()},
                new String[]{"image/jpeg"},
                null
            );

//            Toast.makeText(this, file.getName(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // private void kuaijin() {
    //     long currentPosition = player.getCurrentPosition();
    //     player.seekTo(Math.min(currentPosition + 60_000, player.getDuration()));
    // }

    private void toggleRecording() {
        if (isRecording) {
            stopRecording();
        } else {
            startRecording();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startRecording() {
        if (player == null || playerView == null) {
            Toast.makeText(this, "播放器未初始化", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Toast.makeText(this, "录制功能需要Android 7.0以上", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            View videoSurfaceView = playerView.getVideoSurfaceView();
            if (!(videoSurfaceView instanceof SurfaceView)) {
                Toast.makeText(this, "不支持的视图类型", Toast.LENGTH_SHORT).show();
                return;
            }

            Format videoFormatInfo = player.getVideoFormat();
            if (videoFormatInfo == null) {
                Toast.makeText(this, "无法获取视频信息", Toast.LENGTH_SHORT).show();
                return;
            }

            // 获取音频格式信息
            Format audioFormatInfo = player.getAudioFormat();
            if (audioFormatInfo != null) {
                audioSampleRate = audioFormatInfo.sampleRate;
                audioChannelCount = audioFormatInfo.channelCount;
            }

            int videoWidth = videoFormatInfo.width;
            int videoHeight = videoFormatInfo.height;

            // 确保尺寸是偶数（编码器要求）
            videoWidth = (videoWidth / 2) * 2;
            videoHeight = (videoHeight / 2) * 2;

            // 创建输出文件
            String fileName = "X_VIDEO_" + System.currentTimeMillis() + ".mp4";
            File path = new File(Environment.getExternalStorageDirectory(), "Movies/x");
            if (!path.exists()) {
                path.mkdirs();
            }
            recordingFile = new File(path, fileName);

            // 创建MediaMuxer
            mediaMuxer = new MediaMuxer(recordingFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            // 创建视频编码器
            MediaFormat videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, videoWidth, videoHeight);
            videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, 8 * 1000 * 1000); // 8 Mbps
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            videoEncoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            videoEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            recordingSurface = videoEncoder.createInputSurface();
            videoEncoder.start();

            // 创建音频编码器
            try {
                MediaFormat audioFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, audioSampleRate, audioChannelCount);
                audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
                audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, audioBitrate);
                audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384);

                audioEncoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
                audioEncoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                audioEncoder.start();
            } catch (Exception e) {
                Log.e("PlayActivity2", "创建音频编码器失败: " + e.getMessage());
                // 如果音频编码器创建失败，继续只录制视频
                audioEncoder = null;
            }

            // 初始化音频录制（Android 10+）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && audioEncoder != null) {
                try {
                    initAudioRecord();
                } catch (Exception e) {
                    Log.e("PlayActivity2", "初始化音频录制失败: " + e.getMessage());
                    audioRecord = null;
                }
            }

            // 启动录制线程处理编码输出
            isRecording = true;
            recordingStartTime = System.nanoTime();
            recordingHandler = new Handler(Looper.getMainLooper());

            // 视频编码线程
            recordingThread = new Thread(() -> {
                try {
                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    boolean muxerStarted = false;
                    boolean videoTrackAdded = false;
                    boolean audioTrackAdded = false;

                    while (isRecording) {
                        // 处理视频编码输出
                        int encoderStatus = videoEncoder.dequeueOutputBuffer(bufferInfo, 10000);
                        if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                            // 继续处理音频
                        } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                            MediaFormat newFormat = videoEncoder.getOutputFormat();
                            videoTrackIndex = mediaMuxer.addTrack(newFormat);
                            videoTrackAdded = true;
                            // 如果音频轨道也已添加，启动muxer
                            if (audioTrackAdded || audioEncoder == null) {
                                mediaMuxer.start();
                                muxerStarted = true;
                            }
                            continue;
                        } else if (encoderStatus >= 0) {
                            if (muxerStarted && bufferInfo.size != 0) {
                                ByteBuffer encodedData = videoEncoder.getOutputBuffer(encoderStatus);
                                if (encodedData != null) {
                                    encodedData.position(bufferInfo.offset);
                                    encodedData.limit(bufferInfo.offset + bufferInfo.size);
                                    mediaMuxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo);
                                }
                            }
                            videoEncoder.releaseOutputBuffer(encoderStatus, false);
                        }

                        // 处理音频编码输出
                        if (audioEncoder != null) {
                            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
                            int audioStatus = audioEncoder.dequeueOutputBuffer(audioBufferInfo, 0);
                            if (audioStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                                // 继续
                            } else if (audioStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                                MediaFormat newFormat = audioEncoder.getOutputFormat();
                                audioTrackIndex = mediaMuxer.addTrack(newFormat);
                                audioTrackAdded = true;
                                // 如果视频轨道也已添加，启动muxer
                                if (videoTrackAdded) {
                                    mediaMuxer.start();
                                    muxerStarted = true;
                                }
                            } else if (audioStatus >= 0) {
                                if (muxerStarted && audioBufferInfo.size != 0) {
                                    ByteBuffer encodedData = audioEncoder.getOutputBuffer(audioStatus);
                                    if (encodedData != null) {
                                        encodedData.position(audioBufferInfo.offset);
                                        encodedData.limit(audioBufferInfo.offset + audioBufferInfo.size);
                                        mediaMuxer.writeSampleData(audioTrackIndex, encodedData, audioBufferInfo);
                                    }
                                }
                                audioEncoder.releaseOutputBuffer(audioStatus, false);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(this, "录制出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        stopRecording();
                    });
                }
            });
            recordingThread.start();

            // 启动音频录制线程
            if (audioRecord != null && audioEncoder != null) {
                audioRecordingThread = new Thread(() -> {
                    try {
                        byte[] audioBuffer = new byte[4096];
                        while (isRecording && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                            int bytesRead = audioRecord.read(audioBuffer, 0, audioBuffer.length);
                            if (bytesRead > 0) {
                                int inputBufferIndex = audioEncoder.dequeueInputBuffer(0);
                                if (inputBufferIndex >= 0) {
                                    ByteBuffer inputBuffer = audioEncoder.getInputBuffer(inputBufferIndex);
                                    if (inputBuffer != null) {
                                        inputBuffer.clear();
                                        inputBuffer.put(audioBuffer, 0, bytesRead);
                                        long presentationTimeUs = (System.nanoTime() - recordingStartTime) / 1000;
                                        audioEncoder.queueInputBuffer(inputBufferIndex, 0, bytesRead, presentationTimeUs, 0);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("PlayActivity2", "音频录制线程出错: " + e.getMessage());
                    }
                });
                audioRecordingThread.start();
            }

            // 使用PixelCopy定期复制SurfaceView内容到录制Surface
            startFrameCapture((SurfaceView) videoSurfaceView, videoFormatInfo);

            Toast.makeText(this, "开始录制", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "开始录制失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            isRecording = false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void startFrameCapture(SurfaceView surfaceView, Format format) {
        frameCaptureHandler = new Handler(Looper.getMainLooper());

        Runnable captureRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRecording || recordingSurface == null) {
                    return;
                }

                try {
                    int videoWidth = format.width;
                    int videoHeight = format.height;
                    videoWidth = (videoWidth / 2) * 2;
                    videoHeight = (videoHeight / 2) * 2;

                    Bitmap bitmap = Bitmap.createBitmap(videoWidth, videoHeight, Bitmap.Config.ARGB_8888);

                    PixelCopy.request(
                        surfaceView,
                        bitmap,
                        copyResult -> {
                            if (copyResult == PixelCopy.SUCCESS && isRecording && recordingSurface != null) {
                                // 注意：MediaCodec的Surface是硬件Surface，不支持Canvas直接绘制
                                // 完整的实现需要使用OpenGL/EGL来将Bitmap渲染到编码器Surface
                                // 这里提供一个简化实现，实际使用时可能需要完善OpenGL渲染部分
                                //
                                // 简化方案：由于直接绘制到硬件Surface需要OpenGL，这里我们
                                // 使用一个变通方法 - 创建一个GLSurfaceView或使用EGL渲染
                                //
                                // 注意：以下代码可能不会完全工作，需要OpenGL支持才能完整实现
                                try {
                                    // 尝试使用Canvas（通常不会成功，因为Surface是硬件Surface）
                                    android.graphics.Canvas canvas = recordingSurface.lockCanvas(null);
                                    if (canvas != null) {
                                        canvas.drawBitmap(bitmap, 0, 0, null);
                                        recordingSurface.unlockCanvasAndPost(canvas);
                                    } else {
                                        // Surface不支持Canvas，需要OpenGL渲染
                                        // 在实际应用中，这里应该使用EGL将bitmap渲染到Surface
                                        Log.w("PlayActivity2", "Recording surface requires OpenGL/EGL rendering");
                                    }
                                } catch (Exception e) {
                                    // MediaCodec的Surface是硬件Surface，需要使用OpenGL/EGL来渲染
                                    // 完整的实现需要：
                                    // 1. 创建EGL上下文
                                    // 2. 创建GL纹理并加载Bitmap
                                    // 3. 渲染纹理到编码器Surface
                                    Log.w("PlayActivity2", "Cannot draw to hardware surface directly: " + e.getMessage());
                                }
                                bitmap.recycle();
                            }
                            // 继续下一帧
                            if (isRecording) {
                                frameCaptureHandler.postDelayed(this, 33); // ~30 FPS
                            }
                        },
                        new Handler(Looper.getMainLooper())
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                    if (isRecording) {
                        frameCaptureHandler.postDelayed(this, 33);
                    }
                }
            }
        };

        frameCaptureHandler.post(captureRunnable);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void initAudioRecord() {
        try {
            int channelConfig = audioChannelCount == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
            int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
            int bufferSize = AudioRecord.getMinBufferSize(audioSampleRate, channelConfig, audioFormat);
            if (bufferSize <= 0) {
                bufferSize = audioSampleRate * 2 * audioChannelCount * 2; // 2秒的缓冲区
            }

            // 使用REMOTE_SUBMIX音频源来录制播放的音频（不是麦克风）
            // 注意：这需要系统权限，在某些设备上可能不可用
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ 使用AudioRecord.Builder
                try {
                    audioRecord = new AudioRecord.Builder()
                            .setAudioSource(MediaRecorder.AudioSource.REMOTE_SUBMIX)
                            .setAudioFormat(new AudioFormat.Builder()
                                    .setEncoding(audioFormat)
                                    .setSampleRate(audioSampleRate)
                                    .setChannelMask(channelConfig)
                                    .build())
                            .setBufferSizeInBytes(bufferSize * 2)
                            .build();
                } catch (Exception e) {
                    Log.e("PlayActivity2", "使用AudioRecord.Builder失败，尝试直接创建: " + e.getMessage());
                    // 降级到直接创建
                    audioRecord = new AudioRecord(MediaRecorder.AudioSource.REMOTE_SUBMIX,
                            audioSampleRate, channelConfig, audioFormat, bufferSize * 2);
                }
            } else {
                // Android 9及以下直接创建
                audioRecord = new AudioRecord(MediaRecorder.AudioSource.REMOTE_SUBMIX,
                        audioSampleRate, channelConfig, audioFormat, bufferSize * 2);
            }

            if (audioRecord != null && audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                audioRecord.startRecording();
                Log.d("PlayActivity2", "AudioRecord初始化成功，开始录制播放的音频");
            } else {
                Log.e("PlayActivity2", "AudioRecord初始化失败");
                if (audioRecord != null) {
                    audioRecord.release();
                }
                audioRecord = null;
            }
        } catch (Exception e) {
            Log.e("PlayActivity2", "初始化AudioRecord失败: " + e.getMessage());
            if (audioRecord != null) {
                try {
                    audioRecord.release();
                } catch (Exception e2) {
                    // 忽略释放错误
                }
            }
            audioRecord = null;
        }
    }

    private void stopRecording() {
        if (!isRecording) {
            return;
        }

        isRecording = false;

        // 停止帧捕获
        if (frameCaptureHandler != null) {
            frameCaptureHandler.removeCallbacksAndMessages(null);
        }

        try {
            // 停止音频录制
            if (audioRecord != null) {
                try {
                    if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                        audioRecord.stop();
                    }
                } catch (Exception e) {
                    Log.e("PlayActivity2", "停止AudioRecord失败: " + e.getMessage());
                }
            }

            // 停止音频编码器
            if (audioEncoder != null) {
                try {
                    audioEncoder.signalEndOfInputStream();
                } catch (Exception e) {
                    Log.e("PlayActivity2", "停止音频编码器失败: " + e.getMessage());
                }
            }

            // 停止视频编码器
            if (videoEncoder != null) {
                videoEncoder.signalEndOfInputStream();
            }

            // 等待音频录制线程完成
            if (audioRecordingThread != null) {
                try {
                    audioRecordingThread.join(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                audioRecordingThread = null;
            }

            // 等待录制线程完成
            if (recordingThread != null) {
                try {
                    recordingThread.join(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                recordingThread = null;
            }

            // 释放音频编码器
            if (audioEncoder != null) {
                try {
                    audioEncoder.stop();
                    audioEncoder.release();
                } catch (Exception e) {
                    Log.e("PlayActivity2", "释放音频编码器失败: " + e.getMessage());
                }
                audioEncoder = null;
            }

            // 释放视频编码器
            if (videoEncoder != null) {
                videoEncoder.stop();
                videoEncoder.release();
                videoEncoder = null;
            }

            // 释放AudioRecord
            if (audioRecord != null) {
                try {
                    audioRecord.release();
                } catch (Exception e) {
                    Log.e("PlayActivity2", "释放AudioRecord失败: " + e.getMessage());
                }
                audioRecord = null;
            }

            if (recordingSurface != null) {
                recordingSurface.release();
                recordingSurface = null;
            }

            if (mediaMuxer != null) {
                try {
                    mediaMuxer.stop();
                    mediaMuxer.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mediaMuxer = null;
            }

            if (recordingFile != null && recordingFile.exists()) {
                // 通知媒体库更新
                MediaScannerConnection.scanFile(
                    this,
                    new String[]{recordingFile.getAbsolutePath()},
                    new String[]{"video/mp4"},
                    null
                );
                Toast.makeText(this, "录制已保存: " + recordingFile.getName(), Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "停止录制失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String generateFileName(String url) {
        try {
            // 移除协议部分 (http:// 或 https://)
            String cleanUrl = url.replaceFirst("^(http[s]?://)", "");

            // 移除查询参数
            cleanUrl = cleanUrl.split("\\?")[0];

            // 移除最后的文件扩展名（如.m3u8）
            cleanUrl = cleanUrl.replaceFirst("\\.[^.]+$", "");

            // 只保留路径的最后部分
            String[] parts = cleanUrl.split("/");
            String lastPart = parts[parts.length - 1];

            // 如果最后部分为空，使用倒数第二部分
            if (lastPart.isEmpty() && parts.length > 1) {
                lastPart = parts[parts.length - 2];
            }

            // 如果还是空或太短，使用完整URL的哈希
            if (lastPart.length() < 5) {
                lastPart = String.valueOf(url.hashCode());
                if (lastPart.startsWith("-")) {
                    lastPart = lastPart.substring(1);
                }
            }

            // 添加前缀并确保文件名合法
            String fileName = "X" + lastPart.replaceAll("[^a-zA-Z0-9_-]", "_");

            // 限制文件名长度
            if (fileName.length() > 100) {
                fileName = fileName.substring(0, 100);
            }

            return fileName;
        } catch (Exception e) {
            // 如果出现任何错误，返回基于URL哈希的文件名
            return "X_" + Math.abs(url.hashCode());
        }
    }

}
