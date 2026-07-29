package com.example.fintrack;

import android.view.LayoutInflater;
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
        holder.binding.imgM3ItemIcon.setImageResource(transaction.getIconRes());
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
