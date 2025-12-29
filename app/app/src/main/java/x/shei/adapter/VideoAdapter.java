package x.shei.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import x.shei.R;
import x.shei.activity.PlayActivity;
import x.shei.activity.PlayActivity2;
import x.shei.db.Bean;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ImageViewHolder> {

    private Context context;
    private List<Bean> imageFiles;
    private boolean newPlay;
    private boolean sql;
    private OnItemLongClickListener longClickListener;  // 添加长按监听器

    public interface OnItemLongClickListener {
        boolean onItemLongClick(int position, Bean bean);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }


    public void setSql(Boolean sql) {
        this.sql = sql;
    }

    RequestOptions requestOptions = new RequestOptions()
            .override(1000, 1500)
            .centerCrop()
//            .transform(new CenterCrop())
            .diskCacheStrategy(DiskCacheStrategy.ALL);

    public VideoAdapter(Context context, List<Bean> imageFiles,boolean newPlay) {
        this.context = context;
        this.imageFiles = imageFiles;
        this.newPlay = newPlay;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        ImageViewHolder holder = new ImageViewHolder(view);
//        if (sql) {
//            ViewGroup.LayoutParams params = holder.imageView.getLayoutParams();
////            params.height = dpToPx(context, 120);
////            holder.imageView.setLayoutParams(params);
//            holder.text.setVisibility(View.GONE);
//        }else{
//            holder.text.setVisibility(View.VISIBLE);
//        }
//        holder.text.setVisibility(View.VISIBLE);
        return holder;
    }

    private int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Bean bean = imageFiles.get(position);
        Glide.with(context).load(bean.src).placeholder(R.mipmap.b) .apply(requestOptions)
                .into(holder.imageView);
        holder.text.setText(bean.title);
        holder.text2.setText(bean.updateTime);
        holder.text3.setText(bean.country);
//        holder.text4.setText(bean.mainActor);
//        holder.text5.setText(bean.intro);
        holder.imageView.setOnClickListener(view -> new Thread(() -> {
            if (sql){
                ((Activity) context).runOnUiThread(() -> {
                    Intent intent =new Intent(context, newPlay? PlayActivity2.class:PlayActivity.class);
                    intent.putExtra("m3u8",bean.m3u8);
                    intent.putExtra("src",bean.src);
                    intent.putExtra("a2",bean.a2);
                    intent.putExtra("type",bean.type);
                    intent.putExtra("bean",bean);
                    context.startActivity(intent);
                });
            }
            else{
                String u = extractM3U8Links(bean);
                Log.e("asd","m3u8:"+u);
                ((Activity) context).runOnUiThread(() -> {
                    Intent intent =new Intent(context,newPlay?PlayActivity2.class:PlayActivity.class);
                    intent.putExtra("m3u8",u);
                    intent.putExtra("src",bean.src);
                    intent.putExtra("a2",bean.a2);
                    intent.putExtra("type",bean.type);
                    intent.putExtra("bean",bean);
                    context.startActivity(intent);
                });
            }
        }).start());
        holder.imageView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                return longClickListener.onItemLongClick(position, bean);
            }
            return false;
        });
    }

    public static String extractM3U8Links(Bean bean){
        String m3u8Url = "";
        Log.e("asd","url:"+ bean.a2);
        try {
            Document doc = Jsoup.connect(bean.a2).get();
//            Log.e("asd","doc:"+ doc.toString());
            Elements scripts = doc.select("script");

            for (Element script : scripts) {
                String scriptContent = script.html();
                if (scriptContent.contains("player_aaaa")) {
                    // 使用正则表达式提取 m3u8 URL
                    Pattern pattern = Pattern.compile("\"url\":\"(https:[^\\\"]*\\.m3u8)\"");
                    Matcher matcher = pattern.matcher(scriptContent);
                    if (matcher.find()) {
                        m3u8Url = matcher.group(1);
                        Log.e("asd","m3u8 URL: " + m3u8Url);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        m3u8Url = m3u8Url.replace("\\/", "/");
        bean.m3u8 = m3u8Url;
        return m3u8Url;
    }

    public static String extractM3U8Links2(Bean bean){
        String m3u8Url = "";
        Log.e("asd","url:"+ bean.a2);
        try {
            Document doc = Jsoup.connect(bean.a2).get();
            Log.e("asd","doc:"+ doc.toString());
            // 查找 <script> 标签中的 m3u8 链接
            Elements scripts = doc.select("script");
            for (Element script : scripts) {
                String scriptContent = script.html();
                // 使用正则表达式查找 m3u8 链接
                Pattern pattern = Pattern.compile("https?://[^\\s]+\\.m3u8");
                Matcher matcher = pattern.matcher(scriptContent);
                while (matcher.find()) {
                    m3u8Url = matcher.group();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        bean.m3u8 = m3u8Url;
        return m3u8Url;
    }

    @Override
    public int getItemCount() {
        return imageFiles.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView text;
        TextView text2;
        TextView text3;
        TextView text4;
        TextView text5;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            text = itemView.findViewById(R.id.text);
            text2 = itemView.findViewById(R.id.text2);
            text3 = itemView.findViewById(R.id.text3);
            text4 = itemView.findViewById(R.id.text4);
            text5 = itemView.findViewById(R.id.text5);
        }
    }
}
