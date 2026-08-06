package com.example.fintrack;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fintrack.databinding.ItemTransactionRecentBinding;
import java.util.List;

public class RecentTransactionAdapter extends RecyclerView.Adapter<RecentTransactionAdapter.ViewHolder> {

    private List<Transaction> transactions;

    public RecentTransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransactionRecentBinding binding = ItemTransactionRecentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.binding.txtM3ItemTitle.setText(transaction.getTitle());
        holder.binding.txtM3ItemDate.setText(transaction.getDate());
        holder.binding.txtM3ItemAmount.setText(transaction.getAmount());

        // Ưu tiên hiển thị icon màu sắc từ drawable nếu có tag (đã được map trong M3DashboardFragment)
        String iconEmoji = transaction.getIconEmoji();
        if (iconEmoji != null && !iconEmoji.isEmpty()) {
            // Kiểm tra xem iconEmoji có phải là một tag (food, home, car...) hay không
            int resId = holder.itemView.getContext().getResources().getIdentifier(
                    "ic_" + iconEmoji, "drawable", holder.itemView.getContext().getPackageName());
            
            if (resId != 0) {
                // Nếu tìm thấy icon drawable tương ứng với tag
                holder.binding.imgM3ItemIcon.setVisibility(View.VISIBLE);
                holder.binding.imgM3ItemIcon.setImageResource(resId);
                holder.binding.tvM3ItemEmoji.setVisibility(View.GONE);
            } else {
                // Nếu không tìm thấy, hiển thị như Emoji bình thường
                holder.binding.tvM3ItemEmoji.setVisibility(View.VISIBLE);
                holder.binding.tvM3ItemEmoji.setText(iconEmoji);
                holder.binding.imgM3ItemIcon.setVisibility(View.GONE);
            }
        } else {
            // Logic hiển thị mặc định cũ
            holder.binding.tvM3ItemEmoji.setVisibility(View.GONE);
            holder.binding.imgM3ItemIcon.setVisibility(View.VISIBLE);
            if (transaction.getIconRes() != -1) {
                holder.binding.imgM3ItemIcon.setImageResource(transaction.getIconRes());
            } else {
                holder.binding.imgM3ItemIcon.setImageResource(R.drawable.ic_wallet);
            }
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemTransactionRecentBinding binding;

        public ViewHolder(ItemTransactionRecentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
