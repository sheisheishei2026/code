package x.shei.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import x.shei.db.EntranceItem;
import x.shei.R;

public class EntranceAdapter extends RecyclerView.Adapter<EntranceAdapter.ViewHolder> {
    private final RequestOptions requestOptions;
    private List<EntranceItem> items;
    private Context context;

    public EntranceAdapter(Context context, List<EntranceItem> items) {
        this.context = context;
        this.items = items;

        // 创建圆角变换，设置圆角半径为16dp
        int cornerRadius = (int) (context.getResources().getDisplayMetrics().density * 5);
        requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL) // 使用磁盘缓存
                .centerCrop() // 使用centerCrop以保持图片比例
                .transform(new RoundedCorners(cornerRadius)) // 添加圆角变换
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_error);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.app_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EntranceItem item = items.get(position);
        holder.titleTextView.setText(item.getTitle());
        // 使用 Glide 加载图片，保持原始宽高比
        Glide.with(context)
                .load(item.getIconResId())
                .apply(requestOptions)
                .into(holder.iconImageView);
//        if (item.getIconResId() != 0) {
//            holder.iconImageView.setImageResource(item.getIconResId());
//        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, item.getActivityClass());
            if (item.getTitle().equals("开发")) {
                intent.putExtra("m3u8", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8");
                intent.putExtra("src","1.jpg");
                intent.putExtra("a2","http://www.baidu.com");
                intent.putExtra("type",-1);
            }else if (item.getTitle().equals("视频2")) {
                intent.putExtra("type", 1);
            }else if (item.getTitle().equals("全集2")) {
                intent.putExtra("type", 2);
            }else if (item.getTitle().equals("旅行")) {
                intent.putExtra("url", "https://peizhifei.github.io/uni-travel/#/");
                intent.putExtra("full", true);
            }else if (item.getTitle().equals("计算器")) {
                intent.putExtra("url", "https://peizhifei.github.io/pwa2/cal.html");
                intent.putExtra("full", true);
            }else if (item.getTitle().equals("H5")) {
                intent.putExtra("url", "https://peizhifei.github.io/pwa2/");
                intent.putExtra("full", true);
            }else if (item.getTitle().equals("邮箱")) {
                intent.putExtra("url", "https://wx.mail.qq.com/");
            }
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImageView;
        TextView titleTextView;

        ViewHolder(View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.icon);
            titleTextView = itemView.findViewById(R.id.appName);
        }
    }
}
