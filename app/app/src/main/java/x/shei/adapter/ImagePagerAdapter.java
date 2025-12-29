package x.shei.adapter;

import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;

import x.shei.R;
import x.shei.activity.MediaItem;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {
    private List<MediaItem> imageList;
    private RequestOptions requestOptions;

    public ImagePagerAdapter(List<MediaItem> imageList) {
        this.imageList = imageList != null ? new ArrayList<>(imageList) : new ArrayList<>();
        
        requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_error);
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fullscreen_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        if (position < 0 || position >= imageList.size()) {
            return;
        }
        
        MediaItem mediaItem = imageList.get(position);
        Uri uri = mediaItem.getUri();
        
        if (uri != null) {
            Glide.with(holder.photoView.getContext())
                    .load(uri)
                    .apply(requestOptions)
                    .into(holder.photoView);
        } else {
            String path = mediaItem.getPath();
            if (path != null) {
                try {
                    Uri uriFromString = Uri.parse(path);
                    if (uriFromString.getScheme() != null) {
                        Glide.with(holder.photoView.getContext())
                                .load(uriFromString)
                                .apply(requestOptions)
                                .into(holder.photoView);
                    } else {
                        Glide.with(holder.photoView.getContext())
                                .load(path)
                                .apply(requestOptions)
                                .into(holder.photoView);
                    }
                } catch (Exception e) {
                    Glide.with(holder.photoView.getContext())
                            .load(path)
                            .apply(requestOptions)
                            .into(holder.photoView);
                }
            }
        }
        
        // 设置点击事件关闭Activity
        holder.photoView.setOnClickListener(v -> {
            if (holder.photoView.getContext() instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) holder.photoView.getContext();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    activity.supportFinishAfterTransition();
                } else {
                    activity.finish();
                }
            } else if (holder.photoView.getContext() instanceof android.app.Activity) {
                android.app.Activity activity = (android.app.Activity) holder.photoView.getContext();
                activity.finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        PhotoView photoView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photo_view);
        }
    }
}

