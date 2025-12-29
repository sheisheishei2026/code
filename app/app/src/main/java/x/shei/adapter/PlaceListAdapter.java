package x.shei.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

import x.shei.R;
import x.shei.activity.PoiActivity;
import x.shei.db.PlaceItem;

public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.ViewHolder> {
    private List<PlaceItem> placeList;

    public PlaceListAdapter(List<PlaceItem> placeList) {
        this.placeList = placeList;
    }

    public void updateData(List<PlaceItem> newList) {
        this.placeList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlaceItem place = placeList.get(position);

        // 加载图片
        Glide.with(holder.itemView.getContext())
                .load(place.img)
                .placeholder(R.mipmap.ic_launcher)
                .transform(new RoundedCorners(24))  // 12dp radius * 2 (density)
                .centerCrop()
                .into(holder.placeImage);

        // 设置标题
        holder.placeTitle.setText(place.title);

        // 设置价格
        holder.placePrice.setText(getPrice(place));

        // 显示视频播放图标
        holder.playIcon.setVisibility(!place.video .equals("")  ? View.VISIBLE : View.GONE);

        // 添加标签
        holder.tagContainer.removeAllViews();
        if (!TextUtils.isEmpty(place.recomend)) {
            for (String tag : place.recomend.split("，")) {
                TextView tagView = createTagView(holder.tagContainer, tag);
                holder.tagContainer.addView(tagView);
            }
        }

        ImageView[] stars = new ImageView[5];
        stars[0] = holder.itemView.findViewById(R.id.star1);
        stars[1] = holder.itemView.findViewById(R.id.star2);
        stars[2] = holder.itemView.findViewById(R.id.star3);
        stars[3] = holder.itemView.findViewById(R.id.star4);
        stars[4] = holder.itemView.findViewById(R.id.star5);

        // 设置评分和星级显示
        float rating = Float.parseFloat(place.rate);
        // 设置星星
        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_star_filled);
            } else {
                stars[i].setImageResource(R.drawable.ic_star_empty);
            }
        }

        // 设置预约按钮
        holder.btnReserve.setVisibility(place.recomend!=null&& place.recomend.contains("需预约") ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> {
            try {
                ((PoiActivity) holder.itemView.getContext()).switchMap();
                ((PoiActivity) holder.itemView.getContext()).moveTo(place);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private String getPrice(PlaceItem item) {
        if (item.pricenum != null && !item.pricenum.isEmpty()) {
            if (!item.pricenum.equals("0")) {
                return "¥" + String.format("%.0f", Double.parseDouble(item.pricenum)) + "元";
            } else {
                return "免费";
            }
        } else if (item.price != null && !item.price.equals("0")) {
            return "¥" + String.format("%.0f", Double.parseDouble(item.price)) + "元";
        } else {
            return "免费";
        }
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    private TextView createTagView(ViewGroup parent, String tag) {
        TextView tagView = new TextView(parent.getContext());
        ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 8, 8);
        tagView.setLayoutParams(layoutParams);
        tagView.setPadding(12, 4, 12, 4);
        tagView.setBackgroundResource(R.drawable.tag_background);
        tagView.setText(tag);
        tagView.setTextSize(10);
        tagView.setTextColor(0xFF2196F3);
        return tagView;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView placeImage;
        ImageView playIcon;
        TextView placeTitle;
        TextView placePrice;
        FlexboxLayout tagContainer;
        TextView btnReserve;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            placeImage = itemView.findViewById(R.id.place_image);
            playIcon = itemView.findViewById(R.id.play_icon);
            placeTitle = itemView.findViewById(R.id.place_title);
            placePrice = itemView.findViewById(R.id.place_price);
            tagContainer = itemView.findViewById(R.id.tag_container);
            btnReserve = itemView.findViewById(R.id.btn_reserve);
        }
    }
}
