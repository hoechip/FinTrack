package com.example.fintrack;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fintrack.databinding.ItemEnvelopeBinding;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class EnvelopeAdapter extends RecyclerView.Adapter<EnvelopeAdapter.EnvelopeViewHolder> {

    private final List<Envelope> envelopes;

    public EnvelopeAdapter(List<Envelope> envelopes) {
        this.envelopes = envelopes;
    }

    @NonNull
    @Override
    public EnvelopeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEnvelopeBinding binding = ItemEnvelopeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new EnvelopeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EnvelopeViewHolder holder, int position) {
        holder.bind(envelopes.get(position), position + 1);
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
