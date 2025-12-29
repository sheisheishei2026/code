package x.shei.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import x.shei.activity.PoiActivity;
import x.shei.db.PlaceItem;
import x.shei.fragment.MapFragment;
import x.shei.R;
import x.shei.db.LocationCategory;
import x.shei.fragment.LocationListFragment;

public class LocationCategoryAdapter extends RecyclerView.Adapter<LocationCategoryAdapter.ViewHolder> {
    private Context context;
    private List<LocationCategory> categories;

    public LocationCategoryAdapter(Context context, List<LocationCategory> categories) {
        this.context = context;
        this.categories = categories;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_location_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocationCategory category = categories.get(position);
        holder.tvCategoryTitle.setText(category.getTitle());

        // 清除之前的视图
        holder.llItemsContainer.removeAllViews();

        // 添加横向滚动的项目
        if (category.getNavList() != null) {
            for (PlaceItem place : category.getNavList()) {
                View itemView = LayoutInflater.from(context).inflate(R.layout.item_location, holder.llItemsContainer, false);

                ImageView ivLocation = itemView.findViewById(R.id.iv_location);
                TextView tvLocationName = itemView.findViewById(R.id.tv_location_name);
//                TextView tvLocationRate = itemView.findViewById(R.id.tv_location_rate);

                // 使用Glide加载图片l
//                RequestOptions requestOptions = new RequestOptions()
//                    .transform(new RoundedCorners(50))
//                    .placeholder(R.mipmap.ic_launcher)
//                    .error(R.drawable.ic_launcher);

                // 创建一个 RoundedCorners 对象，指定圆角半径
                RoundedCorners roundedCorners = new RoundedCorners(30);
                RequestOptions requestOptions = RequestOptions.bitmapTransform(roundedCorners)
                        .placeholder(R.mipmap.ic_launcher)
                        .override(400, 300);


                Glide.with(context)
                        .load(place.getImg())
                        .apply(requestOptions).centerCrop()
                        .into(ivLocation);

                tvLocationName.setText(place.getTitle());

                // 设置评分
//                if (!TextUtils.isEmpty(place.getRate())) {
//                    tvLocationRate.setText("评分: " + place.getRate());
//                    tvLocationRate.setVisibility(View.VISIBLE);
//                } else {
//                    tvLocationRate.setVisibility(View.GONE);
//                }

                // 设置描述
//                if (!TextUtils.isEmpty(place.get())) {
//                    tvLocationDescription.setText(place.getDescription());
//                    tvLocationDescription.setVisibility(View.VISIBLE);
//                } else {
//                    tvLocationDescription.setVisibility(View.GONE);
//                }

                // 添加点击事件
                itemView.setOnClickListener(v -> {
                    try {
                        ((PoiActivity) context).switchMap();
                        ((PoiActivity) context).moveTo(place);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                holder.llItemsContainer.addView(itemView);
            }
        }


    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryTitle;
        LinearLayout llItemsContainer;

        ViewHolder(View itemView) {
            super(itemView);
            tvCategoryTitle = itemView.findViewById(R.id.tv_category_title);
            llItemsContainer = itemView.findViewById(R.id.ll_items_container);
        }
    }
}
