package x.shei.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.List;

import x.shei.activity.FullscreenImageActivity;
import x.shei.R;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private Context context;
    private List<File> imageUris;
    private OnItemDeleteListener deleteListener;
    private RequestOptions requestOptions;

    public interface OnItemDeleteListener {
        void onDelete(int position, File file);
    }

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.deleteListener = listener;
    }

    public ImageAdapter(Context context, List<File> imageUris) {
        this.context = context;
        this.imageUris = imageUris;

        // 配置 Glide 加载选项
        requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL) // 使用磁盘缓存
                .fitCenter() // 使用 fitCenter 而不是 centerCrop
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_error);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File imageUri = imageUris.get(position);

        // 使用 Glide 加载图片，保持原始宽高比
        Glide.with(context)
                .load(imageUri)
                .apply(requestOptions)
                .into(holder.imageView);

        // 设置长按监听
        holder.itemView.setOnLongClickListener(v -> {
            showDeleteDialog(position, imageUri);
            return true;
        });

        holder.itemView.setOnClickListener(v -> {
            // 启动全屏查看图片的 Activity
            Intent intent = new Intent(context, FullscreenImageActivity.class);
            intent.putExtra("image_url", imageUri.getAbsolutePath());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        (android.app.Activity) context,
                        holder.imageView,
                        ViewCompat.getTransitionName(holder.imageView));
                context.startActivity(intent, options.toBundle());
            } else {
                context.startActivity(intent);
            }
        });
    }

    private void showDeleteDialog(int position, File file) {
        new AlertDialog.Builder(context)
                .setTitle("删除确认")
                .setMessage("确定要删除这张图片吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    if (deleteListener != null) {
                        deleteListener.onDelete(position, file);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public void removeItem(int position) {
        imageUris.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, imageUris.size());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
