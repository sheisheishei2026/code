package x.shei.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import x.shei.R;
import x.shei.db.movie.MovieDownload;
import x.shei.db.movie.MovieWithDownloads;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private Context context;
    private List<MovieWithDownloads> movies;

    public MovieAdapter(Context context, List<MovieWithDownloads> movies) {
        this.context = context;
        this.movies = movies;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        MovieWithDownloads movie = movies.get(position);

        Glide.with(context)
            .load(movie.movie.img)
            .into(holder.imageView);

        holder.titleText.setText(movie.movie.title);
        holder.actorText.setText("演员: " + movie.movie.actor);
        holder.areaText.setText("地区: " + movie.movie.area);
        holder.contentText.setText(movie.movie.content);

        // 清除之前的下载按钮
        holder.downloadsContainer.removeAllViews();

        // 添加下载按钮
        for (MovieDownload download : movie.downloads) {
            Button button = new Button(context);
            button.setText(download.name);
            button.setOnClickListener(v -> {
                // TODO: 处理下载点击
            });
            holder.downloadsContainer.addView(button);
        }
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleText;
        TextView actorText;
        TextView areaText;
        TextView contentText;
        LinearLayout downloadsContainer;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            titleText = itemView.findViewById(R.id.titleText);
            actorText = itemView.findViewById(R.id.actorText);
            areaText = itemView.findViewById(R.id.areaText);
            contentText = itemView.findViewById(R.id.contentText);
            downloadsContainer = itemView.findViewById(R.id.downloadsContainer);
        }
    }
}
