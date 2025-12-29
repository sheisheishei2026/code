package x.shei.util;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import x.shei.R;
public class FileViewHolder extends RecyclerView.ViewHolder {
    public TextView fileNameTextView;
    public TextView fileTypeTextView;
    public  TextView url;
    public   ImageView imageView;

    public FileViewHolder(@NonNull View itemView) {
        super(itemView);
        fileNameTextView = itemView.findViewById(R.id.fileNameTextView);
        fileTypeTextView = itemView.findViewById(R.id.fileTypeTextView);
        url = itemView.findViewById(R.id.url);
        imageView = itemView.findViewById(R.id.imageView);
    }
}
