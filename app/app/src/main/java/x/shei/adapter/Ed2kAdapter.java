package x.shei.adapter;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.List;

import x.shei.db.DataObject;
import x.shei.R;

public class Ed2kAdapter extends RecyclerView.Adapter<Ed2kAdapter.ViewHolder> {
    private final ClipboardManager clipboard;
    private Context context;
    private List<DataObject> imageUris;
    private OnItemDeleteListener deleteListener;
    private RequestOptions requestOptions;
    private int selectedPosition = -1; // 用于跟踪选中的项

    public interface OnItemDeleteListener {
        void onDelete(int position, File file);
    }

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.deleteListener = listener;
    }

    public Ed2kAdapter(Context context, List<DataObject> imageUris) {
        this.context = context;
        this.imageUris = imageUris;
        clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        // 配置 Glide 加载选项
//        requestOptions = new RequestOptions()
//                .diskCacheStrategy(DiskCacheStrategy.ALL) // 使用磁盘缓存
//                .fitCenter() // 使用 fitCenter 而不是 centerCrop
//                .placeholder(R.drawable.image_placeholder)
//                .error(R.drawable.image_error);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_data, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataObject imageUri = imageUris.get(position);
        String da = imageUri.getPart2();
        if (TextUtils.isEmpty(da)){
            holder.imageView.setText(imageUri.getPart1());
        } else{
            holder.imageView.setText(imageUri.getPart2());
        }


        if (selectedPosition == position) {
            holder.imageView.setBackgroundColor(Color.RED);
        } else {
            holder.imageView.setBackgroundColor(Color.parseColor("#272A2C"));
        }

        holder.itemView.setOnClickListener(v -> {
//            ClipData clip = ClipData.newPlainText("url", imageUri.getPart2());
//            clipboard.setPrimaryClip(clip);
            try{
                int previousSelectedPosition = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(previousSelectedPosition);
                notifyItemChanged(selectedPosition);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(imageUri.getPart1()));
                context.startActivity(intent);
            } catch (ActivityNotFoundException e){
                e.printStackTrace();
                Toast.makeText(context, "请安装ed2k下载软件", Toast.LENGTH_SHORT).show();
            }
        });


        // 设置长按监听
//        holder.itemView.setOnLongClickListener(v -> {
//            showDeleteDialog(position, imageUri);
//            return true;
//        });
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
        TextView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
