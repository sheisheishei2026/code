package x.shei.util;

import android.content.Context;
import android.content.Intent;

import x.shei.service.AudioPlaybackService;

public class AudioServiceHelper {
    
    /**
     * 启动音频播放服务
     * @param context 上下文
     */
    public static void startAudioService(Context context) {
        Intent intent = new Intent(context, AudioPlaybackService.class);
        try {
            context.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 停止音频播放服务
     * @param context 上下文
     */
    public static void stopAudioService(Context context) {
        Intent intent = new Intent(context, AudioPlaybackService.class);
        try {
            context.stopService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}