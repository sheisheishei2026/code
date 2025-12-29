package x.shei.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import x.shei.db.Bean;
import x.shei.activity.PlayActivity;
import x.shei.R;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class VAdapter extends RecyclerView.Adapter<VAdapter.ViewHolder> {
    private Context context;
    private List<Bean> videoUris;
    private RequestOptions requestOptions;
    private ExoPlayer currentPlayer;

    private VideoAdapter.OnItemLongClickListener longClickListener;  // 添加长按监听器

    public void setOnItemLongClickListener(VideoAdapter.OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public VAdapter(Context context, List<Bean> videoUris) {
        this.context = context;
        this.videoUris = videoUris;

        // 配置 Glide 加载选项
        requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL) // 使用磁盘缓存
                .fitCenter() // 使用 fitCenter 而不是 centerCrop
                .placeholder(R.mipmap.a)
                .error(R.mipmap.a);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bean videoUri = videoUris.get(position);

        // 使用 Glide 加载图片，保持原始宽高比
        Glide.with(context)
                .load(videoUri.src)
                .apply(requestOptions)
                .into(holder.imageView);

        // 设置播放按钮点击监听
        holder.playButton.setOnClickListener(v -> {
            if (currentPlayer != null) {
                currentPlayer.stop();
                currentPlayer.release();
            }
            currentPlayer = new ExoPlayer.Builder(context).build();
            holder.playerView.setPlayer(currentPlayer);
            MediaItem mediaItem = MediaItem.fromUri(videoUri.m3u8);
            currentPlayer.setMediaItem(mediaItem);
            currentPlayer.prepare();
            currentPlayer.play();
            holder.imageView.setVisibility(View.GONE);
            holder.playButton.setVisibility(View.GONE);
            holder.playerView.setVisibility(View.VISIBLE);
        });

        // 设置播放器控制栏按钮的点击事件
        View fullscreenButton = holder.playerView.findViewById(R.id.exo_download);
        View copyButton = holder.playerView.findViewById(R.id.exo_copy);
        View screenshotButton = holder.playerView.findViewById(R.id.exo_screenshot);
        View favoriteButton = holder.playerView.findViewById(R.id.exo_favorite);
        favoriteButton.setVisibility(View.GONE);

        if (fullscreenButton != null) {
            fullscreenButton.setOnClickListener(view -> {
                // 启动全屏播放Activity
                Intent intent = new Intent(context, PlayActivity.class);
                intent.putExtra("m3u8", videoUri.m3u8);
                intent.putExtra("a2", videoUri.a2);
                intent.putExtra("src", videoUri.src);
                intent.putExtra("type", videoUri.type);
                context.startActivity(intent);
            });
        }

        if (copyButton != null) {
            copyButton.setOnClickListener(view -> {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("url", videoUri.m3u8);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
            });
        }

        if (screenshotButton != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            screenshotButton.setOnClickListener(view -> {
                // 截图功能实现
                View videoSurfaceView = holder.playerView.getVideoSurfaceView();
                if (videoSurfaceView instanceof SurfaceView) {
                    takeScreenshot(holder.playerView, currentPlayer, (SurfaceView) videoSurfaceView);
                }
            });
        }

        if (favoriteButton != null) {
            favoriteButton.setOnClickListener(view -> {
//                        // 收藏功能实现
//                        FavDatabase favDb = FavDatabase.getInstance(context);
//                        FavDao favDao = favDb.favDao();
//                        new Thread(() -> {
//                            try {
//                                FavEntity fav = new FavEntity(videoUri.src, videoUri.m3u8, videoUri.a2, videoUri.type);
//                                long result = favDao.insert(fav);
//                                new Handler(Looper.getMainLooper()).post(() -> {
//                                    if (result != -1) {
//                                        Toast.makeText(context, "收藏成功", Toast.LENGTH_SHORT).show();
//                                    } else {
//                                        Toast.makeText(context, "已经收藏过了", Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                new Handler(Looper.getMainLooper()).post(() -> {
//                                    Toast.makeText(context, "收藏失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                                });
//                            }
//                        }).start();
            });
        }


        holder.imageView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                return longClickListener.onItemLongClick(position, videoUri);
            }
            return false;
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void takeScreenshot(PlayerView playerView, ExoPlayer player, SurfaceView surfaceView) {
        try {
            Format format = player.getVideoFormat();
            if (format == null) {
                Toast.makeText(context, "无法获取视频信息", Toast.LENGTH_SHORT).show();
                return;
            }

            int videoWidth = format.width;
            int videoHeight = format.height;
            float videoRatio = (float) videoWidth / videoHeight;

            int surfaceWidth = surfaceView.getWidth();
            int surfaceHeight = surfaceView.getHeight();
            float surfaceRatio = (float) surfaceWidth / surfaceHeight;

            int targetWidth, targetHeight;
            if (surfaceRatio > videoRatio) {
                targetHeight = surfaceHeight;
                targetWidth = (int) (surfaceHeight * videoRatio);
            } else {
                targetWidth = surfaceWidth;
                targetHeight = (int) (surfaceWidth / videoRatio);
            }

            Bitmap bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
            PixelCopy.request(
                    surfaceView,
                    bitmap,
                    copyResult -> {
                        if (copyResult == PixelCopy.SUCCESS) {
                            saveScreenshot(bitmap);
                        } else {
                            Toast.makeText(context, "截图失败", Toast.LENGTH_SHORT).show();
                        }
                    },
                    new Handler(Looper.getMainLooper())
            );
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "截图失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

            MediaScannerConnection.scanFile(
                    context,
                    new String[]{file.getAbsolutePath()},
                    new String[]{"image/jpeg"},
                    null
            );
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return videoUris.size();
    }

    public void removeItem(int position) {
        videoUris.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, videoUris.size());
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.playerView.getPlayer() != null) {
            holder.playerView.getPlayer().stop();
            holder.playerView.getPlayer().release();
            holder.playerView.setPlayer(null);
            holder.imageView.setVisibility(View.VISIBLE);
            holder.playButton.setVisibility(View.VISIBLE);
            holder.playerView.setVisibility(View.GONE);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView playButton;
        PlayerView playerView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            playButton = itemView.findViewById(R.id.playButton);
            playerView = itemView.findViewById(R.id.playerView);
        }
    }
}
