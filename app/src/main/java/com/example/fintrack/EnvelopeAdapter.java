package com.example.fintrack;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fintrack.databinding.ItemEnvelopeBinding;
import java.text.NumberFormat;
import com.example.fintrack.databinding.ItemM4EnvelopeBinding;
import java.util.List;
import java.util.Locale;

public class EnvelopeAdapter extends RecyclerView.Adapter<EnvelopeAdapter.EnvelopeViewHolder> {
public class EnvelopeAdapter extends RecyclerView.Adapter<EnvelopeAdapter.ViewHolder> {

    private final List<Envelope> envelopes;
    private List<Envelope> envelopes;
    private OnEnvelopeActionListener listener;

    public EnvelopeAdapter(List<Envelope> envelopes) {
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
    public EnvelopeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEnvelopeBinding binding = ItemEnvelopeBinding.inflate(
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemM4EnvelopeBinding binding = ItemM4EnvelopeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new EnvelopeViewHolder(binding);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EnvelopeViewHolder holder, int position) {
        holder.bind(envelopes.get(position), position + 1);
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

    static class EnvelopeViewHolder extends RecyclerView.ViewHolder {
        private final ItemEnvelopeBinding binding;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemM4EnvelopeBinding binding;

        public EnvelopeViewHolder(ItemEnvelopeBinding binding) {
        public ViewHolder(ItemM4EnvelopeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Envelope envelope, int rank) {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

            binding.txtItemEnvelopeName.setText(rank + ". " + envelope.getName());
            binding.txtItemEnvelopeAmount.setText(currencyFormat.format(envelope.getSpentAmount()));
            binding.txtItemEnvelopePercent.setText(envelope.getPercent() + "%");

            binding.pbItemEnvelopeProgress.setProgress(envelope.getPercent());

            // Set Icon
            int iconResId = binding.getRoot().getContext().getResources().getIdentifier(
                    envelope.getIconName(), "drawable", binding.getRoot().getContext().getPackageName());
            if (iconResId != 0) {
                binding.imgItemEnvelopeIcon.setImageResource(iconResId);
            }

            if (envelope.getColorHex() != null) {
                int color = Color.parseColor(envelope.getColorHex());
                binding.pbItemEnvelopeProgress.setIndicatorColor(color);
                binding.cardItemIconContainer.setCardBackgroundColor(adjustAlpha(color, 0.2f));
            }
        }

        private int adjustAlpha(int color, float factor) {
            int alpha = Math.round(Color.alpha(color) * factor);
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            return Color.argb(alpha, red, green, blue);
        }
    }
}
