package x.shei.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import x.shei.R;
import x.shei.service.AudioPlaybackService;

public class SoundEffectsAdapter extends RecyclerView.Adapter<SoundEffectsAdapter.SoundEffectViewHolder> {

    private List<SoundEffect> soundEffects;
    private SoundEffectsActivity activity;
    private AudioPlaybackService audioService;

    public SoundEffectsAdapter(List<SoundEffect> soundEffects, SoundEffectsActivity activity) {
        this.soundEffects = soundEffects;
        this.activity = activity;
    }

    public void setAudioService(AudioPlaybackService audioService) {
        this.audioService = audioService;
    }

    @NonNull
    @Override
    public SoundEffectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sound_effect, parent, false);
        return new SoundEffectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SoundEffectViewHolder holder, int position) {
        SoundEffect soundEffect = soundEffects.get(position);
        holder.bind(soundEffect);
    }

    @Override
    public int getItemCount() {
        return soundEffects.size();
    }

    public void stopAllSounds() {
        if (audioService != null) {
            audioService.stopAllSounds();
        }
    }

    class SoundEffectViewHolder extends RecyclerView.ViewHolder {
        private Button button;

        public SoundEffectViewHolder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.btnSoundEffect);
        }

        public void bind(SoundEffect soundEffect) {
            String buttonText = soundEffect.getEmoji() + "\n" + soundEffect.getName();
            button.setText(buttonText);

            // 设置按钮颜色（可以根据需要调整）
            int[] colors = {
                0xFF42A5F5, // 蓝色
                0xFFFF7043, // 橙色
                0xFFFFCA28, // 黄色
                0xFF66BB6A, // 绿色
                0xFFAB47BC, // 紫色
                0xFF78909C, // 灰色
                0xFF26A69A, // 青色
                0xFF7E57C2, // 深紫色
                0xFFFF5722, // 深橙色
                0xFF5C6BC0  // 深蓝色
            };

            int colorIndex = getAdapterPosition() % colors.length;
            button.setBackgroundColor(colors[colorIndex]);

            button.setOnClickListener(v -> playSound(soundEffect));
        }

        private void playSound(SoundEffect soundEffect) {
            try {
                if (audioService != null) {
                    String soundId = "sound_effect_" + soundEffect.getResourceId();
                    boolean shouldLoop = activity.isLoopModeEnabled();

                    // 检查是否正在播放相同音频

                    // 使用服务播放音频
                    audioService.playSoundEffectWithLoop(soundEffect.getResourceId(), soundId, shouldLoop);

                    String loopStatus = shouldLoop ? " (循环)" : "";
//                    Toast.makeText(button.getContext(), "正在播放: " + soundEffect.getName() + loopStatus, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(button.getContext(), "音频服务未连接", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(button.getContext(), "播放失败: " + soundEffect.getName(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
