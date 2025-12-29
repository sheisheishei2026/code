package x.shei.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import x.shei.R;
import x.shei.activity.FullscreenImageActivity;
import x.shei.activity.MediaGalleryActivity;
import x.shei.activity.MediaItem;
import x.shei.activity.PlayActivity;

public class MediaGroupAdapter extends RecyclerView.Adapter<MediaGroupAdapter.GroupViewHolder> {
    private Context context;
    private List<MediaGalleryActivity.MediaGroup> groups;

    public MediaGroupAdapter(Context context, List<MediaGalleryActivity.MediaGroup> groups) {
        this.context = context;
        this.groups = groups != null ? groups : new ArrayList<>();
    }

    public void updateData(List<MediaGalleryActivity.MediaGroup> groups) {
        this.groups = groups != null ? groups : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_media_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        MediaGalleryActivity.MediaGroup group = groups.get(position);
        holder.tvGroupTitle.setText(group.name);

        // 设置横向RecyclerView
        HorizontalMediaAdapter adapter = new HorizontalMediaAdapter(context, group.files);
        holder.recyclerViewHorizontal.setLayoutManager(
            new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerViewHorizontal.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupTitle;
        RecyclerView recyclerViewHorizontal;

        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupTitle = itemView.findViewById(R.id.tvGroupTitle);
            recyclerViewHorizontal = itemView.findViewById(R.id.recyclerViewHorizontal);
        }
    }

    private static class HorizontalMediaAdapter extends RecyclerView.Adapter<HorizontalMediaAdapter.MediaViewHolder> {
        private Context context;
        private List<MediaItem> mediaFiles;
        private RequestOptions requestOptions;

        HorizontalMediaAdapter(Context context, List<MediaItem> mediaFiles) {
            this.context = context;
            this.mediaFiles = mediaFiles != null ? new ArrayList<>(mediaFiles) : new ArrayList<>();

            requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_error);
        }

        @NonNull
        @Override
        public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_media_horizontal, parent, false);
            return new MediaViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
            MediaItem mediaItem = mediaFiles.get(position);

            // 显示播放图标（如果是视频）
            holder.ivPlayIcon.setVisibility(mediaItem.isVideo ? View.VISIBLE : View.GONE);

            // 加载图片或视频缩略图
            Glide.with(context)
                .load(mediaItem.getSource())
                .apply(requestOptions)
                .into(holder.imageView);

            holder.itemView.setOnClickListener(v -> {
                try {
                    if (mediaItem.isVideo) {
                        // 播放视频
                        Intent intent = new Intent(context, PlayActivity.class);
                        String path = mediaItem.getPath();
                        if (path != null) {
                            intent.putExtra("m3u8", path);
                            intent.putExtra("type", 0);
                            context.startActivity(intent);
                        }
                    } else {
                        // 查看图片 - 传递该组的所有图片列表
                        Intent intent = new Intent(context, FullscreenImageActivity.class);
                        // 只传递图片，过滤掉视频
                        ArrayList<MediaItem> imageList = new ArrayList<>();
                        for (MediaItem item : mediaFiles) {
                            if (!item.isVideo) {
                                imageList.add(item);
                            }
                        }
                        intent.putParcelableArrayListExtra("image_list", imageList);
                        // 找到当前图片在列表中的位置
                        int currentPos = 0;
                        for (int i = 0; i < imageList.size(); i++) {
                            if (imageList.get(i).equals(mediaItem)) {
                                currentPos = i;
                                break;
                            }
                        }
                        intent.putExtra("current_position", currentPos);
                        // 添加Uri读取权限标志
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                        // 检查transitionName是否存在
                        String transitionName = ViewCompat.getTransitionName(holder.imageView);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && transitionName != null) {
                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                (android.app.Activity) context,
                                holder.imageView,
                                transitionName);
                            context.startActivity(intent, options.toBundle());
                        } else {
                            context.startActivity(intent);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    android.widget.Toast.makeText(context, "打开失败: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mediaFiles.size();
        }

        static class MediaViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ImageView ivPlayIcon;

            MediaViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
                ivPlayIcon = itemView.findViewById(R.id.ivPlayIcon);
            }
        }
    }
}

