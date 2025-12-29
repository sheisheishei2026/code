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
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import x.shei.R;
import x.shei.activity.WebQQ;
import x.shei.db.DataObject;

public class YouAdapter extends RecyclerView.Adapter<YouAdapter.ViewHolder> {
    private List<DataObject> items;
    private Context context;
    private RequestOptions requestOptions;

    public YouAdapter(Context context, List<DataObject> items) {
        this.context = context;
        this.items = items;
        this.requestOptions = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.mipmap.ic_launcher)
//            .error(R.drawable.error)
            .override(1000, 500);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_you, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataObject item = items.get(position);
        holder.titleTextView.setText(item.title);
//        holder.descriptionTextView.setText(item.description);

        Glide.with(holder.itemView.getContext())
            .load(item.getImg())
            .apply(requestOptions)
            .thumbnail(0.1f)
            .into(holder.iconImageView);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, WebQQ.class);
            intent.putExtra("url", item.getPart2());
            intent.putExtra("full", true);
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
        TextView descriptionTextView;

        ViewHolder(View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.iconImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
//            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
        }
    }
}
