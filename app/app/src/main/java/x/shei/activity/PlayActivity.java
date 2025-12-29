package x.shei.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
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
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import x.shei.R;
import x.shei.db.FavDatabase;
import x.shei.db.FavDao;
import x.shei.db.FavEntity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PlayActivity extends Activity {

    private PlayerView playerView;
    private ExoPlayer player;
    private float touchStartX;
    private float touchStartY;
    private static final int SWIPE_THRESHOLD = 50;  // 最小滑动阈值
    private boolean isSliding = false;  // 是否正在滑动
    private ClipboardManager clipboard;
    private String m3u8;
    private int type;
    private String a2;
    private String src;

    private boolean wasPlaying = false;  // 添加标记记录播放状态

    private FavDatabase favDb;
    private FavDao favDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video);

        // 初始化视图和设置点击监听
        initViews();

        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        m3u8 = getIntent().getStringExtra("m3u8");
        a2 = getIntent().getStringExtra("a2");
        src = getIntent().getStringExtra("src");
        type = getIntent().getIntExtra("type",-1);
        if (m3u8 != null) {
            Log.e("asd",m3u8);
            playerView = findViewById(R.id.player_view);

            // 初始化ExoPlayer
            player = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(player);

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
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            touchStartX = event.getX();
                            touchStartY = event.getY();
                            isSliding = false;
                            return false;

                        case MotionEvent.ACTION_MOVE:
                            float deltaX = event.getX() - touchStartX;
                            float deltaY = event.getY() - touchStartY;

                            // 判断是否为水平滑动
                            if (Math.abs(deltaX) > Math.abs(deltaY) &&
                                Math.abs(deltaX) > SWIPE_THRESHOLD) {
                                isSliding = true;
                                return true;  // 开始滑动后拦截事件
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
                                return true;
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
        // View fastForwardButton = findViewById(R.id.fast_forward_button);
        // View recordButton = findViewById(R.id.record_button);

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
        // fastForwardButton.setOnClickListener(v -> kuaijin());
        // recordButton.setOnClickListener(v -> record());
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 检查屏幕方向
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 横屏时全屏播放
            if (getActionBar() != null) {
                getActionBar().hide();
            }
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 竖屏时退出全屏
            if (getActionBar() != null) {
                getActionBar().show();
            }
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
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
        Intent intent  =new Intent(this,PlayActivity.class);
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

    // private void record() {
    //     // Implementation for record functionality
    // }

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
