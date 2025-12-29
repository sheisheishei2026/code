package x.shei.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import x.shei.R;

public class AudioPlaybackService extends Service {
    private static final String TAG = "AudioPlaybackService";

    // Binder类用于Activity与Service通信
    public class AudioPlaybackBinder extends Binder {
        public AudioPlaybackService getService() {
            return AudioPlaybackService.this;
        }
    }

    private final IBinder binder = new AudioPlaybackBinder();

    // 音频播放器映射
    private ConcurrentHashMap<String, MediaPlayer> mediaPlayerMap = new ConcurrentHashMap<>();
    private AudioManager audioManager;
    private Handler handler = new Handler();

    // 防空警报相关变量
    private Runnable alarmRunnable;
    private boolean isPlaying = false;
    private int currentAlarmType = 0; // 0: none, 1: pre-alarm, 2: air-raid, 3: all-clear

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        Log.d(TAG, "AudioPlaybackService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 保持服务运行
        return START_STICKY; // 服务被杀死后会自动重启
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service bound");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Service unbound");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAllSounds();
        Log.d(TAG, "AudioPlaybackService destroyed");
    }

    // 播放音效页面的音频
    public void playSoundEffect(int resourceId, String soundId) {
        stopMediaPlayer(soundId); // 先停止相同ID的播放器

        MediaPlayer newPlayer = MediaPlayer.create(this, resourceId);
        if (newPlayer != null) {
            // 保存播放器实例
            mediaPlayerMap.put(soundId, newPlayer);

            // 设置播放完成监听器，播放完成后释放资源
            newPlayer.setOnCompletionListener(mp -> {
                mp.release();
                mediaPlayerMap.remove(soundId);
            });

            newPlayer.start();
        }
    }

    // 播放带循环模式的音效
    public void playSoundEffectWithLoop(int resourceId, String soundId, boolean shouldLoop) {
        // 检查是否正在播放相同的音频
        MediaPlayer currentPlayer = mediaPlayerMap.get(soundId);

        if (currentPlayer != null && currentPlayer.isPlaying()) {
            // 如果正在播放，则停止并释放
            currentPlayer.stop();
            currentPlayer.release();
            mediaPlayerMap.remove(soundId);

            // 如果当前播放的音频就是点击的音频，则停止播放，不重新开始
            return;
        }

        MediaPlayer newPlayer = MediaPlayer.create(this, resourceId);
        if (newPlayer != null) {

            // 保存播放器实例
            mediaPlayerMap.put(soundId, newPlayer);

            // 设置循环模式
            newPlayer.setLooping(shouldLoop);

            // 设置播放完成监听器
            newPlayer.setOnCompletionListener(mp -> {
                if (!shouldLoop) {
                    // 非循环模式，播放完成后释放资源
                    mp.release();
                    mediaPlayerMap.remove(soundId);
                }
                // 循环模式下，MediaPlayer会自动重新播放，不需要手动处理
            });

            newPlayer.start();
        }
    }

    // 播放防空警报 - 预先警报
    public void playPreAlarm() {
        if (isPlaying) {
            stopAllSounds();
        }

        currentAlarmType = 1;
        isPlaying = true;
        Toast.makeText(this, "正在播放: 预先警报 (鸣36秒，停24秒，循环3次)", Toast.LENGTH_SHORT).show();
        playPreAlarmSequence();
    }

    // 播放防空警报 - 空袭警报
    public void playAirRaid() {
        if (isPlaying) {
            stopAllSounds();
        }

        currentAlarmType = 2;
        isPlaying = true;
        Toast.makeText(this, "正在播放: 空袭警报 (鸣6秒，停6秒，循环15次)", Toast.LENGTH_SHORT).show();
        playAirRaidSequence();
    }

    // 播放防空警报 - 解除警报
    public void playAllClear() {
        if (isPlaying) {
            stopAllSounds();
        }

        currentAlarmType = 3;
        isPlaying = true;
        Toast.makeText(this, "正在播放: 解除警报 (连续鸣响3分钟)", Toast.LENGTH_SHORT).show();
        playContinuousSound(180000); // 3分钟 = 180000毫秒
    }

    // 预先警报：鸣36秒，停24秒，反复3遍
    private void playPreAlarmSequence() {
        // 计算需要播放多少次12秒的音频来达到36秒
        int soundLoops = 3; // 3次12秒音频 ≈ 36秒
        playTimedSequence(soundLoops, 24000, 3, "预先警报");
    }

    // 空袭警报：鸣6秒，停6秒，反复15遍
    private void playAirRaidSequence() {
        // 由于音频是12秒，我们不能精确播放6秒，所以播放一次后暂停6秒
        playShortSequence(6000, 6000, 15, "空袭警报");
    }

    // 按指定时长播放音频（截取部分播放）
    private void playShortSequence(int soundDuration, int pauseDuration, int cycles, String alarmName) {
        if (cycles <= 0 || !isPlaying || currentAlarmType != (alarmName.contains("预先") ? 1 :
                alarmName.contains("空袭") ? 2 : 3)) {
            return;
        }

        String alarmId = "air_raid_alarm";
        MediaPlayer currentAlarmPlayer = mediaPlayerMap.get(alarmId);

        if (currentAlarmPlayer != null) {
            currentAlarmPlayer.release();
            mediaPlayerMap.remove(alarmId);
        }

        // 创建MediaPlayer并设置播放6秒后停止
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.kongxi2);
        if (mediaPlayer != null) {
            mediaPlayerMap.put(alarmId, mediaPlayer);
            mediaPlayer.start();

            // 设置在指定时间后停止
            handler.postDelayed(() -> {
                MediaPlayer player = mediaPlayerMap.get(alarmId);
                if (player != null && player.isPlaying()) {
                    player.stop();
                    player.release();
                    mediaPlayerMap.remove(alarmId);
                }

                // 等待暂停时间后继续下一轮
                if (isPlaying && currentAlarmType == (alarmName.contains("预先") ? 1 :
                        alarmName.contains("空袭") ? 2 : 3)) {
                    handler.postDelayed(() -> {
                        playShortSequence(soundDuration, pauseDuration, cycles - 1, alarmName);
                    }, pauseDuration);
                }
            }, soundDuration);
        } else {
            Toast.makeText(this, "音效文件不存在: " + alarmName, Toast.LENGTH_LONG).show();
            isPlaying = false;
        }
    }

    // 按循环次数播放音频
    private void playTimedSequence(int soundLoops, int pauseDuration, int cycles, String alarmName) {
        if (cycles <= 0 || !isPlaying || currentAlarmType != (alarmName.contains("预先") ? 1 :
                alarmName.contains("空袭") ? 2 : 3)) {
            return;
        }

        // 连续播放指定次数的音频
        playSoundLoops(soundLoops, 0, () -> {
            // 播放完成后等待暂停时间
            handler.postDelayed(() -> {
                if (isPlaying && currentAlarmType == (alarmName.contains("预先") ? 1 :
                        alarmName.contains("空袭") ? 2 : 3)) {
                    playTimedSequence(soundLoops, pauseDuration, cycles - 1, alarmName);
                }
            }, pauseDuration);
        });
    }

    // 循环播放音频指定次数
    private void playSoundLoops(int totalLoops, int currentLoop, Runnable onComplete) {
        if (currentLoop >= totalLoops || !isPlaying) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        String alarmId = "pre_alarm_" + currentLoop;
        MediaPlayer currentAlarmPlayer = mediaPlayerMap.get(alarmId);

        if (currentAlarmPlayer != null) {
            currentAlarmPlayer.release();
            mediaPlayerMap.remove(alarmId);
        }

        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.kongxi2);
        if (mediaPlayer != null) {
            mediaPlayerMap.put(alarmId, mediaPlayer);
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                mediaPlayerMap.remove(alarmId);
                // 继续播放下一个循环
                playSoundLoops(totalLoops, currentLoop + 1, onComplete);
            });
        } else {
            Toast.makeText(this, "音效文件不存在", Toast.LENGTH_LONG).show();
            isPlaying = false;
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    // 持续播放音频
    private void playContinuousSound(int duration) {
        String alarmId = "all_clear_alarm";
        MediaPlayer currentAlarmPlayer = mediaPlayerMap.get(alarmId);

        if (currentAlarmPlayer != null) {
            currentAlarmPlayer.release();
            mediaPlayerMap.remove(alarmId);
        }

        // 播放声音并循环
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.kongxi2);
        if (mediaPlayer != null) {
            mediaPlayerMap.put(alarmId, mediaPlayer);
            mediaPlayer.setLooping(true); // 设置循环播放
            mediaPlayer.start();

            // 在指定时间后停止
            handler.postDelayed(() -> {
                if (isPlaying && currentAlarmType == 3) {
                    stopMediaPlayer(alarmId);
                }
            }, duration);
        } else {
            Toast.makeText(this, "音效文件不存在: 解除警报", Toast.LENGTH_LONG).show();
            isPlaying = false;
        }
    }

    // 停止指定的音频播放器
    public void stopMediaPlayer(String soundId) {
        MediaPlayer mediaPlayer = mediaPlayerMap.get(soundId);
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayerMap.remove(soundId);
        }
    }

    // 停止所有音频
    public void stopAllSounds() {
        isPlaying = false;
        currentAlarmType = 0;

        // 停止所有播放器
        for (String soundId : mediaPlayerMap.keySet()) {
            MediaPlayer mediaPlayer = mediaPlayerMap.get(soundId);
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            }
        }
        mediaPlayerMap.clear();

        // 移除所有待处理的回调
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

//        Toast.makeText(this, "已停止所有音频", Toast.LENGTH_SHORT).show();
    }

    // 检查是否有正在播放的音频
    public boolean isAnyAudioPlaying() {
        return !mediaPlayerMap.isEmpty() || isPlaying;
    }
}
