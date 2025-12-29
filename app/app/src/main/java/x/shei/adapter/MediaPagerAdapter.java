package x.shei.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.List;

import x.shei.R;
import x.shei.model.PlaceMediaItem;

public class MediaPagerAdapter extends RecyclerView.Adapter<MediaPagerAdapter.ViewHolder> {
    private List<PlaceMediaItem> mediaItems;
    private OnItemClickListener listener;
    private ExoPlayer currentPlayer;

    public interface OnItemClickListener {
        void onItemClick(PlaceMediaItem item);
    }

    public MediaPagerAdapter(List<PlaceMediaItem> mediaItems, OnItemClickListener listener) {
        this.mediaItems = mediaItems;
        this.listener = listener;
    }

    public ExoPlayer getCurrentPlayer() {
        return currentPlayer;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_media, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlaceMediaItem item = mediaItems.get(position);
        Context context = holder.itemView.getContext();

        RequestOptions requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.mipmap.ic_launcher)
                .centerCrop();

        if (item.isVideo()) {
            // 显示视频播放器
            holder.playerView.setVisibility(View.VISIBLE);
            // 设置圆角半径
            float radius = 30;

            holder.playerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // 移除监听器，避免重复调用
                    holder.playerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    // 设置自定义的 ViewOutlineProvider
                    holder.playerView.setOutlineProvider(new TopRoundedCornersOutlineProvider(radius));
                    // 裁剪视图以显示圆角
                    holder.playerView.setClipToOutline(true);
                }
            });

            holder.imageView.setVisibility(View.GONE);
            holder.playButton.setVisibility(View.GONE);

            // 初始化ExoPlayer
            if (holder.player == null) {
                holder.player = new ExoPlayer.Builder(context).build();
                holder.playerView.setPlayer(holder.player);
                currentPlayer = holder.player;  // Store the current player
            }

            // 准备视频
            MediaItem mediaItem = MediaItem.fromUri(item.getUrl());
            holder.player.setMediaItem(mediaItem);
            holder.player.prepare();
            holder.player.setPlayWhenReady(false);

            // 添加播放状态监听
            holder.player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == Player.STATE_ENDED) {
                        holder.player.seekTo(0);
                        holder.player.setPlayWhenReady(false);
                    }
                }
            });

        } else {
            // 显示图片
            holder.playerView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
            holder.playButton.setVisibility(View.GONE);

            // 释放播放器资源
            if (holder.player != null) {
                holder.player.release();
                holder.player = null;
            }

            Glide.with(context)
                    .load(item.getUrl())
                    .apply(requestOptions)
                    .into(holder.imageView);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        // 释放播放器资源
        if (holder.player != null) {
            holder.player.release();
            holder.player = null;
        }
    }

    @Override
    public int getItemCount() {
        return mediaItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView playButton;
        PlayerView playerView;
        ExoPlayer player;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.media_image);
            playButton = itemView.findViewById(R.id.play_button);
            playerView = itemView.findViewById(R.id.player_view);
        }
    }

    private static class TopRoundedCornersOutlineProvider extends ViewOutlineProvider {
        private final float radius;

        public TopRoundedCornersOutlineProvider(float radius) {
            this.radius = radius;
        }

        @Override
        public void getOutline(View view, android.graphics.Outline outline) {
            int width = view.getWidth();
            int height = view.getHeight();
            android.graphics.Rect rect = new android.graphics.Rect(0, 0, width, height);
            // 创建一个 Path 来定义圆角矩形
            android.graphics.Path path = new android.graphics.Path();
            path.addRoundRect(new android.graphics.RectF(rect), new float[]{radius, radius, radius, radius, 0, 0, 0, 0}, android.graphics.Path.Direction.CW);
            // 将 Path 设置到 Outline
            outline.setConvexPath(path);
        }
    }
}
