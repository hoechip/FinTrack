package com.example.fintrack;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fintrack.databinding.ItemM4EnvelopeBinding;
import java.util.List;

public class EnvelopeAdapter extends RecyclerView.Adapter<EnvelopeAdapter.ViewHolder> {

    private List<Envelope> envelopes;
    private OnEnvelopeActionListener listener;

    public interface OnEnvelopeActionListener {
        void onEdit(Envelope envelope, int position);
        void onDelete(Envelope envelope, int position);
    }

    public EnvelopeAdapter(List<Envelope> envelopes, OnEnvelopeActionListener listener) {
        this.envelopes = envelopes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemM4EnvelopeBinding binding = ItemM4EnvelopeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Envelope item = envelopes.get(position);
        holder.binding.txtM4EnvelopeName.setText(item.getName());
        holder.binding.imgM4EnvelopeIcon.setImageResource(item.getIconRes());

        holder.binding.btnM4EnvelopeEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(item, position);
        });

        holder.binding.btnM4EnvelopeDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item, position);
        });
    }

    @Override
    public int getItemCount() {
        return envelopes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemM4EnvelopeBinding binding;

        public ViewHolder(ItemM4EnvelopeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
