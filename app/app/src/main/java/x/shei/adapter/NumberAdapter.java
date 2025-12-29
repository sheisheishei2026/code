package x.shei.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import x.shei.R;

public class NumberAdapter extends RecyclerView.Adapter<NumberAdapter.NumberViewHolder> {

    private final List<Integer> numbers;
    private final OnItemClickListener listener;
    private int selectedPosition = -1; // 记录选中的位置

    public interface OnItemClickListener {
        void onItemClick(int number);
    }

    public NumberAdapter(List<Integer> numbers, OnItemClickListener listener) {
        this.numbers = numbers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NumberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_number, parent, false);
        return new NumberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NumberViewHolder holder, int position) {
        holder.bind(numbers.get(position), listener, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return numbers.size();
    }

    // 添加选中方法
    public void setSelected(int position) {
        int previousSelected = selectedPosition;
        selectedPosition = position;

        // 更新之前选中的项和新选中的项
        if (previousSelected != -1) {
            notifyItemChanged(previousSelected);
        }
        notifyItemChanged(selectedPosition);
    }

    static class NumberViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public NumberViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.number);
        }

        public void bind(final Integer number, final OnItemClickListener listener, boolean isSelected) {
            textView.setText(String.valueOf(number));
            textView.setSelected(isSelected); // 设置选中状态

            itemView.setOnClickListener(v -> {
                listener.onItemClick(number);
                // 通知适配器更新选中状态
                if (getBindingAdapter() instanceof NumberAdapter) {
                    ((NumberAdapter) getBindingAdapter()).setSelected(getBindingAdapterPosition());
                }
            });
        }
    }
}
