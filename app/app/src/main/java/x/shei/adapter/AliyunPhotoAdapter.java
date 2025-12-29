package x.shei.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import x.shei.R;
import x.shei.db.AliyunPhoto;

import java.util.List;

public class AliyunPhotoAdapter extends RecyclerView.Adapter<AliyunPhotoAdapter.ViewHolder> {
    private Context context;
    private List<AliyunPhoto> photos;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(AliyunPhoto photo);
    }

    public AliyunPhotoAdapter(Context context, List<AliyunPhoto> photos) {
        this.context = context;
        this.photos = photos;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AliyunPhoto photo = photos.get(position);

        Glide.with(context)
                .load(photo.getThumbnailUrl())
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_error)
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(photo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
