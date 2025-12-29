package x.shei.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

import x.shei.R;
import x.shei.util.ImmersedUtil;

public class EmergencyActivity extends BaseActivity {

    private static final int REQUEST_SMS_PERMISSION = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;

    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;
    private MediaPlayer mediaPlayer1, mediaPlayer2, mediaPlayer3, mediaPlayer4; // 用于混响效果
    private SharedPreferences sharedPreferences;
    private android.speech.tts.TextToSpeech textToSpeech;
    private boolean ttsInitialized = false;
    private android.location.LocationManager locationManager;
    private String locationProvider = android.location.LocationManager.NETWORK_PROVIDER;

    // 长按音量键检测相关变量
    private static final int LONG_PRESS_DELAY = 1000; // 1秒
    private Handler volumeKeyHandler = new Handler();
    private Runnable volumeKeyRunnable;
    private boolean isVolumeUpPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);
        ImmersedUtil.setImmersedMode(this, false);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        sharedPreferences = getSharedPreferences("EmergencySettings", MODE_PRIVATE);
        locationManager = (android.location.LocationManager) getSystemService(LOCATION_SERVICE);

        // 初始化TTS
        initializeTTS();

        initViews();
    }

    private void initViews() {
        Button btnEmergencyCall110 = findViewById(R.id.btnEmergencyCall110);
        Button btnEmergencyCall120 = findViewById(R.id.btnEmergencyCall120);
        Button btnEmergencyCall119 = findViewById(R.id.btnEmergencyCall119);
        Button btnWarningSound = findViewById(R.id.btnWarningSound);
        Button btnSendSms = findViewById(R.id.btnSendSms);
        Button btnSettings = findViewById(R.id.btnSettings);

        // 添加新按钮
        Button btnAlarmSound = findViewById(R.id.btnAlarmSound);
        Button btnReverbSound = findViewById(R.id.btnReverbSound);

        btnEmergencyCall110.setOnClickListener(v -> makeEmergencyCall("110"));
        btnEmergencyCall120.setOnClickListener(v -> makeEmergencyCall("120"));
        btnEmergencyCall119.setOnClickListener(v -> makeEmergencyCall("119"));
        btnWarningSound.setOnClickListener(v -> playWarningSound());
        btnSendSms.setOnClickListener(v -> sendEmergencySms());
        btnSettings.setOnClickListener(v -> openSettings());

        // 新按钮点击事件
        btnAlarmSound.setOnClickListener(v -> playAlarmSound());
        btnReverbSound.setOnClickListener(v -> playReverbSound());
    }

    private void makeEmergencyCall(String phoneNumber) {
        // 使用 ACTION_DIAL 打开拨号界面而不是直接拨打电话
        Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
        startActivity(callIntent);
    }

    private void playWarningSound() {
        // 获取用户设置的警告文字，如果没有则使用默认值
        String warningText = sharedPreferences.getString("warning_text", "着火了着火了");

        // 如果是默认值且用户从未设置过，提示用户设置
        if (warningText.equals("着火了着火了")) {
            Toast.makeText(this, "请先到设置页面配置警告内容", Toast.LENGTH_LONG).show();
            openSettings();
            return;
        }

        // 调大音量到最大
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);

        // 使用TTS播放警告内容
        if (ttsInitialized) {
            // 在Android 8.0及以上使用QUEUE_FLUSH模式，其他版本使用适当的模式
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                textToSpeech.speak(warningText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "warning");
            } else {
                textToSpeech.speak(warningText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null);
            }
//            Toast.makeText(this, "正在播放警告: " + warningText, Toast.LENGTH_SHORT).show();
        } else {
            // 如果TTS未初始化，尝试重新初始化
            initializeTTS();

            // 如果仍然未初始化，使用系统默认方式提示用户
            if (!ttsInitialized) {
                // 尝试使用Intent启动系统TTS设置
                Intent intent = new Intent();
                intent.setAction("com.android.settings.TTS_SETTINGS");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                    Toast.makeText(this, "设备缺少TTS服务，已跳转到TTS设置页面，请安装TTS引擎", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "设备缺少TTS服务，请在系统设置中安装TTS引擎", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void initializeTTS() {
        if (textToSpeech == null) {
            textToSpeech = new android.speech.tts.TextToSpeech(this, status -> {
                if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(java.util.Locale.getDefault());
                    if (result == android.speech.tts.TextToSpeech.LANG_MISSING_DATA ||
                        result == android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED) {
                        // 如果默认语言不支持，尝试使用中文
                        result = textToSpeech.setLanguage(java.util.Locale.CHINESE);
                        if (result == android.speech.tts.TextToSpeech.LANG_MISSING_DATA ||
                            result == android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED) {
                            result = textToSpeech.setLanguage(java.util.Locale.SIMPLIFIED_CHINESE);
                            if (result == android.speech.tts.TextToSpeech.LANG_MISSING_DATA ||
                                result == android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED) {
                                // 如果所有中文语言都不支持，尝试英文
                                result = textToSpeech.setLanguage(java.util.Locale.ENGLISH);
                                if (result == android.speech.tts.TextToSpeech.LANG_MISSING_DATA ||
                                    result == android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED) {
                                    Toast.makeText(this, "TTS语言数据不支持，请检查系统TTS服务", Toast.LENGTH_SHORT).show();
                                    ttsInitialized = false;
                                    return;
                                }
                            }
                        }
                    }
                    ttsInitialized = true;
//                    Toast.makeText(this, "TTS服务初始化成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "TTS初始化失败", Toast.LENGTH_SHORT).show();
                    ttsInitialized = false;
                }
            });
        }
    }

    private void sendEmergencySms() {
        String phoneNumber = sharedPreferences.getString("emergency_phone", "");

        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "请先到设置页面配置紧急联系人手机号", Toast.LENGTH_LONG).show();
            openSettings();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS_PERMISSION);
            return;
        }

        // 获取当前位置信息
        String location = getCurrentLocation();
        String message = "当前有事，位置是" + location;

        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber));
        smsIntent.putExtra("sms_body", message);
        startActivity(smsIntent);
    }

    private String getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return "位置信息"; // 权限未获取时返回默认值
        }

        try {
            android.location.Location location = locationManager.getLastKnownLocation(locationProvider);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                return "纬度:" + latitude + ", 经度:" + longitude;
            } else {
                return "无法获取位置信息";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "位置获取失败";
        }
    }

    private void openSettings() {
        Intent intent = new Intent(this, EmergencySettingsActivity.class);
        startActivity(intent);
    }

    private void playAlarmSound() {
        // 播放m5.mp3文件
        try {
            // 调大音量到最大
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);

            // 停止之前的播放
            stopMediaPlayer(mediaPlayer);

            try {
                // 尝试使用raw资源中的音频文件
                int resId = R.raw.m5;
                if (resId != 0) {
                    mediaPlayer = MediaPlayer.create(this, resId);
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    if (mediaPlayer != null) {
                        mediaPlayer.setLooping(true);
                        mediaPlayer.start();
//                        Toast.makeText(this, "正在播放警报声", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "音频文件不存在", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "音频文件不存在，请添加m5.mp3到assets文件夹或res/raw文件夹", Toast.LENGTH_LONG).show();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(this, "播放音频失败: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void playReverbSound() {
        // 同时播放m1-m4这4个mp3文件
        try {
            // 调大音量到最大
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);

            // 停止之前的播放
            stopMediaPlayer(mediaPlayer1);
            stopMediaPlayer(mediaPlayer2);
            stopMediaPlayer(mediaPlayer3);
            stopMediaPlayer(mediaPlayer4);

            // 播放m1-m4音频文件
            playAudioFile("m1.mp3", mediaPlayer1, 1);
            playAudioFile("m2.mp3", mediaPlayer2, 2);
            playAudioFile("m3.mp3", mediaPlayer3, 3);
            playAudioFile("m4.mp3", mediaPlayer4, 4);

//            Toast.makeText(this, "正在播放混响声", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "播放混响声失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void playAudioFile(String fileName, MediaPlayer existingPlayer, int playerIndex) {
        try {
            MediaPlayer player;
            if (existingPlayer != null) {
                player = existingPlayer;
                player.reset();
            } else {
                player = new MediaPlayer();
            }

            // 设置数据源
            android.content.res.AssetFileDescriptor afd = getAssets().openFd(fileName);
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setLooping(true); // 循环播放

            player.prepare();
            player.start();

            // 保存引用
            switch (playerIndex) {
                case 1:
                    mediaPlayer1 = player;
                    break;
                case 2:
                    mediaPlayer2 = player;
                    break;
                case 3:
                    mediaPlayer3 = player;
                    break;
                case 4:
                    mediaPlayer4 = player;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();

            // 尝试使用raw资源
            try {
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                int resId = getResources().getIdentifier(baseName, "raw", getPackageName());
                if (resId != 0) {
                    MediaPlayer newPlayer = MediaPlayer.create(this, resId);
                    if (newPlayer != null) {
                        newPlayer.setLooping(true);
                        newPlayer.start();

                        // 保存引用
                        switch (playerIndex) {
                            case 1:
                                mediaPlayer1 = newPlayer;
                                break;
                            case 2:
                                mediaPlayer2 = newPlayer;
                                break;
                            case 3:
                                mediaPlayer3 = newPlayer;
                                break;
                            case 4:
                                mediaPlayer4 = newPlayer;
                                break;
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void stopMediaPlayer(MediaPlayer player) {
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
        }
    }

    private void stopAllAudio() {
        stopMediaPlayer(mediaPlayer);
        stopMediaPlayer(mediaPlayer1);
        stopMediaPlayer(mediaPlayer2);
        stopMediaPlayer(mediaPlayer3);
        stopMediaPlayer(mediaPlayer4);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAllAudio();

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_SMS_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "短信权限已获取", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "需要短信权限才能发送短信", Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "位置权限已获取", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "需要位置权限才能获取当前位置", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (!isVolumeUpPressed) {
                isVolumeUpPressed = true;
                // 设置长按检测的Runnable
                volumeKeyRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (isVolumeUpPressed) {
                            // 长按音量上键超过1秒，跳转到EmergencyActivity
                            Intent intent = new Intent(EmergencyActivity.this, SoundEffectsActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
//                            Toast.makeText(EmergencyActivity.this, "长按音量上键 - 跳转到紧急页面", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                // 延迟执行Runnable
                volumeKeyHandler.postDelayed(volumeKeyRunnable, LONG_PRESS_DELAY);
            }
            return true; // 拦截事件
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (isVolumeUpPressed) {
                isVolumeUpPressed = false;
                // 取消长按检测的Runnable
                if (volumeKeyRunnable != null) {
                    volumeKeyHandler.removeCallbacks(volumeKeyRunnable);
                }
            }
            return true; // 拦截事件
        }
        return super.onKeyUp(keyCode, event);
    }
}
