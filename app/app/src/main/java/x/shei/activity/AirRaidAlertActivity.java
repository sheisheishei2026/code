package x.shei.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import x.shei.R;
import x.shei.service.AudioPlaybackService;
import x.shei.util.ImmersedUtil;

public class AirRaidAlertActivity extends AppCompatActivity {

    private Handler handler;
    private AudioPlaybackService audioService;
    private boolean serviceBound = false;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioPlaybackService.AudioPlaybackBinder binder =
                (AudioPlaybackService.AudioPlaybackBinder) service;
            audioService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_raid_alert);
        ImmersedUtil.setImmersedMode(this, false);

        handler = new Handler();

        initViews();

        // 绑定音频播放服务
        Intent intent = new Intent(this, AudioPlaybackService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void initViews() {
        Button btnPreAlarm = findViewById(R.id.btnPreAlarm);
        Button btnAirRaid = findViewById(R.id.btnAirRaid);
        Button btnAllClear = findViewById(R.id.btnAllClear);
        Button btnStopAll = findViewById(R.id.btnStopAll);

        btnPreAlarm.setOnClickListener(v -> playPreAlarmSound());
        btnAirRaid.setOnClickListener(v -> playAirRaidSound());
        btnAllClear.setOnClickListener(v -> playAllClearSound());
        btnStopAll.setOnClickListener(v -> stopAllSounds());
    }

    private void playPreAlarmSound() {
        if (audioService != null) {
            audioService.playPreAlarm();
        } else {
            Toast.makeText(this, "音频服务未连接", Toast.LENGTH_SHORT).show();
        }
    }

    private void playAirRaidSound() {
        if (audioService != null) {
            audioService.playAirRaid();
        } else {
            Toast.makeText(this, "音频服务未连接", Toast.LENGTH_SHORT).show();
        }
    }

    private void playAllClearSound() {
        if (audioService != null) {
            audioService.playAllClear();
        } else {
            Toast.makeText(this, "音频服务未连接", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAllSounds() {
        if (audioService != null) {
            audioService.stopAllSounds();
        }
        handler.removeCallbacksAndMessages(null);
//        Toast.makeText(this, "已停止所有警报", Toast.LENGTH_SHORT).show();
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
