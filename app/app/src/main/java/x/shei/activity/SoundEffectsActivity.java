package x.shei.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import x.shei.R;
import x.shei.service.AudioPlaybackService;
import x.shei.util.ImmersedUtil;

public class SoundEffectsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SoundEffectsAdapter adapter;
    private Button btnStopAll;
    private Button btnToggleLoop;
    private boolean isLoopMode = false;

    // 音频播放服务相关
    private AudioPlaybackService audioService;
    private boolean serviceBound = false;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioPlaybackService.AudioPlaybackBinder binder =
                (AudioPlaybackService.AudioPlaybackBinder) service;
            audioService = binder.getService();
            serviceBound = true;
            // 通知adapter服务已连接
            if (adapter != null) {
                adapter.setAudioService(audioService);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_effects_dynamic);
        ImmersedUtil.setImmersedMode(this, false);

        initViews();
        setupRecyclerView();

        // 绑定音频播放服务
        Intent intent = new Intent(this, AudioPlaybackService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewSoundEffects);
        btnStopAll = findViewById(R.id.btnStopAll);
        btnToggleLoop = findViewById(R.id.btnToggleLoop);

        btnStopAll.setOnClickListener(v -> stopAllSounds());
        btnToggleLoop.setOnClickListener(v -> toggleLoopMode());
    }

    private void setupRecyclerView() {
        // 初始化音效数据列表
        List<SoundEffect> soundEffects = new ArrayList<>();

        soundEffects.add(new SoundEffect(R.raw.dianhua, "电话", "📞"));
        soundEffects.add(new SoundEffect(R.raw.wxlaidian, "微信来电", "💬"));
        soundEffects.add(new SoundEffect(R.raw.veribly, "震动", "📳"));

        soundEffects.add(new SoundEffect(R.raw.cough, "咳嗽", "😷"));
        soundEffects.add(new SoundEffect(R.raw.fangpi, "放屁", "💨"));
        soundEffects.add(new SoundEffect(R.raw.dahulu, "打呼噜", "😴"));

        soundEffects.add(new SoundEffect(R.raw.goujiao, "狗叫", "🐕"));
        soundEffects.add(new SoundEffect(R.raw.tiger, "老虎", "🐅"));
        soundEffects.add(new SoundEffect(R.raw.go, "go", "🏁"));

        soundEffects.add(new SoundEffect(R.raw.laughter, "笑声", "😄"));
        soundEffects.add(new SoundEffect(R.raw.manlaugh2, "单人笑", "😆"));
        soundEffects.add(new SoundEffect(R.raw.manlaugh, "单人笑2", "😂"));

        soundEffects.add(new SoundEffect(R.raw.qiaomen, "敲门", "🚪"));
        soundEffects.add(new SoundEffect(R.raw.qiaomen2, "敲门2", "🚪"));
        soundEffects.add(new SoundEffect(R.raw.jianpan, "机械键盘", "⌨️"));

//        soundEffects.add(new SoundEffect(R.raw.typing, "打字", "⌨️"));
//        soundEffects.add(new SoundEffect(R.raw.huaixiao, "坏笑", "😏"));

        soundEffects.add(new SoundEffect(R.raw.kongxi, "空袭", "💣"));
        soundEffects.add(new SoundEffect(R.raw.m1, "警报1", "🚨"));
        soundEffects.add(new SoundEffect(R.raw.m2, "警报2", "⚠️"));
        soundEffects.add(new SoundEffect(R.raw.m3, "警报3", "🔔"));
        soundEffects.add(new SoundEffect(R.raw.m4, "警报4", "📢"));
        soundEffects.add(new SoundEffect(R.raw.m5, "警报5", "🎯"));

        soundEffects.add(new SoundEffect(R.raw.fangpao, "放炮", "🧨"));
        soundEffects.add(new SoundEffect(R.raw.yanhua, "烟花", "🎆"));
        soundEffects.add(new SoundEffect(R.raw.guonian, "过年", "🏮"));

        soundEffects.add(new SoundEffect(R.raw.manchuan, "喘气", "😮‍💨"));
        soundEffects.add(new SoundEffect(R.raw.dahai, "大海", "🌊"));
        soundEffects.add(new SoundEffect(R.raw.music1, "音乐", "🎵"));

        // 可以继续添加更多音效
        // soundEffects.add(new SoundEffect(R.raw.xxx, "名称", "emoji"));

        // 设置RecyclerView
        adapter = new SoundEffectsAdapter(soundEffects, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // 3列网格布局
        recyclerView.setAdapter(adapter);
    }

    private void stopAllSounds() {
        if (adapter != null) {
            adapter.stopAllSounds();
//            Toast.makeText(this, "已停止所有音效", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleLoopMode() {
        isLoopMode = !isLoopMode;
        btnToggleLoop.setText(isLoopMode ? "🔄 循环播放: 开" : "🔄 循环播放: 关");
//        btnToggleLoop.setBackgroundColor(isLoopMode ? 0xFF4CAF50 : 0xFFFF9800); // 绿色表示开启，橙色表示关闭

//        Toast.makeText(this, "循环播放已" + (isLoopMode ? "开启" : "关闭"), Toast.LENGTH_SHORT).show();
    }

    public boolean isLoopModeEnabled() {
        return isLoopMode;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAllSounds();

        // 解绑服务
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }
}
