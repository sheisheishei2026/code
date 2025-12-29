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
import x.shei.db.SmsInfo;

public class SmsAdapter extends RecyclerView.Adapter<SmsAdapter.ViewHolder> {

    private List<SmsInfo> smsList;
    private Context context;
    private OnSmsActionListener listener;

    public interface OnSmsActionListener {
        void onViewAll(SmsInfo smsInfo);
        void onDelete(SmsInfo smsInfo, int position);
    }

    public SmsAdapter(List<SmsInfo> smsList, Context context) {
        this.smsList = smsList;
        this.context = context;
    }

    public void setOnSmsActionListener(OnSmsActionListener listener) {
        this.listener = listener;
    }

    public void setSmsList(List<SmsInfo> smsList) {
        this.smsList = smsList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < smsList.size()) {
            smsList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, smsList.size());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sms, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SmsInfo smsInfo = smsList.get(position);
        holder.tvAddress.setText(smsInfo.getAddress());
        holder.tvBody.setText(smsInfo.getBody());
        
        // 格式化时间
        String dateStr = DateFormat.format("yyyy-MM-dd HH:mm:ss", smsInfo.getDate()).toString();
        holder.tvDate.setText(dateStr);

        // 查看全部按钮点击事件
        holder.tvViewAll.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewAll(smsInfo);
            }
        });

        // 删除按钮点击事件
        holder.tvDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(smsInfo, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return smsList != null ? smsList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAddress;
        TextView tvBody;
        TextView tvDate;
        TextView tvViewAll;
        TextView tvDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvViewAll = itemView.findViewById(R.id.tvViewAll);
            tvDelete = itemView.findViewById(R.id.tvDelete);
        }
    }
}

