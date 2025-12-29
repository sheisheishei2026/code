package x.shei.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import x.shei.R;
import x.shei.db.PdfInfo;

public class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.ViewHolder> {

    private List<PdfInfo> pdfList;
    private Context context;
    private OnPdfClickListener listener;

    public interface OnPdfClickListener {
        void onPdfClick(PdfInfo pdfInfo);
    }

    public PdfAdapter(List<PdfInfo> pdfList, Context context) {
        this.pdfList = pdfList;
        this.context = context;
    }

    public void setOnPdfClickListener(OnPdfClickListener listener) {
        this.listener = listener;
    }

    public void setPdfList(List<PdfInfo> pdfList) {
        this.pdfList = pdfList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pdf, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PdfInfo pdfInfo = pdfList.get(position);
        holder.tvPdfName.setText(pdfInfo.getName());
        holder.tvPdfSize.setText(pdfInfo.getFormattedSize());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPdfClick(pdfInfo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pdfList != null ? pdfList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPdfIcon;
        TextView tvPdfName;
        TextView tvPdfSize;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPdfIcon = itemView.findViewById(R.id.tvPdfIcon);
            tvPdfName = itemView.findViewById(R.id.tvPdfName);
            tvPdfSize = itemView.findViewById(R.id.tvPdfSize);
        }
    }
}

