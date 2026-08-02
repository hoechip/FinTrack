package com.example.fintrack;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fintrack.databinding.ItemEnvelopeBinding;
import com.example.fintrack.databinding.ItemM4EnvelopeBinding;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class EnvelopeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Envelope> envelopes;
    private OnEnvelopeActionListener listener;

    public interface OnEnvelopeActionListener {
        void onEdit(Envelope envelope, int position);
        void onDelete(Envelope envelope, int position);
    }

    // Constructor for StatisticsFragment
    public EnvelopeAdapter(List<Envelope> envelopes) {
        this.envelopes = envelopes;
    }

    // Constructor for M4EnvelopesFragment
    public EnvelopeAdapter(List<Envelope> envelopes, OnEnvelopeActionListener listener) {
        this.envelopes = envelopes;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return (listener != null) ? 1 : 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            ItemM4EnvelopeBinding binding = ItemM4EnvelopeBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        } else {
            ItemEnvelopeBinding binding = ItemEnvelopeBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new EnvelopeViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Envelope item = envelopes.get(position);
        if (holder instanceof ViewHolder) {
            ((ViewHolder) holder).bind(item, position, listener);
        } else if (holder instanceof EnvelopeViewHolder) {
            ((EnvelopeViewHolder) holder).bind(item, position + 1);
        }
    }

    @Override
    public int getItemCount() {
        return envelopes.size();
    }

    static class EnvelopeViewHolder extends RecyclerView.ViewHolder {
        private final ItemEnvelopeBinding binding;

        public EnvelopeViewHolder(ItemEnvelopeBinding binding) {
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
            if (envelope.getIconName() != null) {
                int iconResId = binding.getRoot().getContext().getResources().getIdentifier(
                        envelope.getIconName(), "drawable", binding.getRoot().getContext().getPackageName());
                if (iconResId != 0) {
                    binding.imgItemEnvelopeIcon.setImageResource(iconResId);
                }
            } else if (envelope.getIconRes() != 0) {
                binding.imgItemEnvelopeIcon.setImageResource(envelope.getIconRes());
            }

            if (envelope.getColorHex() != null) {
                try {
                    int color = Color.parseColor(envelope.getColorHex());
                    binding.pbItemEnvelopeProgress.setIndicatorColor(color);
                    binding.cardItemIconContainer.setCardBackgroundColor(adjustAlpha(color, 0.2f));
                } catch (Exception ignored) {}
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemM4EnvelopeBinding binding;

        public ViewHolder(ItemM4EnvelopeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Envelope item, int position, OnEnvelopeActionListener listener) {
            binding.txtM4EnvelopeName.setText(item.getName());
            binding.imgM4EnvelopeIcon.setImageResource(item.getIconRes());

            binding.btnM4EnvelopeEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(item, position);
            });

            binding.btnM4EnvelopeDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(item, position);
            });
        }
    }
}
