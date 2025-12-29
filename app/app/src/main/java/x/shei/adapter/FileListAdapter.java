package x.shei.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import java.util.Map;

import x.shei.util.FileViewHolder;
import x.shei.R;

public class FileListAdapter extends RecyclerView.Adapter<FileViewHolder> {
    private List<Map<String, Object>> fileInfoList;
    private OnItemClickListener listener;
    private Context context;
    private RequestOptions requestOptions;

    public FileListAdapter(Context context, List<Map<String, Object>> fileInfoList, OnItemClickListener listener) {
        this.context = context;
        this.fileInfoList = fileInfoList;
        this.listener = listener;
        // 配置 Glide 加载选项
        requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL) // 使用磁盘缓存
                .fitCenter() // 使用 fitCenter 而不是 centerCrop
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_error);
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_list, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        Map<String, Object> fileInfo = fileInfoList.get(position);
        String fileName = (String) fileInfo.get("name");
        String fileType = (String) fileInfo.get("type");
        String url = (String) fileInfo.get("url");

        holder.fileNameTextView.setText(fileName);
        holder.fileTypeTextView.setText(fileType);
        holder.url.setText(url);
        // 使用 Glide 加载图片，保持原始宽高比
        Glide.with(context)
                .load(url)
                .apply(requestOptions)
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(fileInfo));
    }

    @Override
    public int getItemCount() {
        return fileInfoList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Map<String, Object> fileInfo);
    }
}
