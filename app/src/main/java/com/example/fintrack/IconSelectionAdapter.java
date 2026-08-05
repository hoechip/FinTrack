package com.example.fintrack;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class IconSelectionAdapter extends RecyclerView.Adapter<IconSelectionAdapter.IconViewHolder> {

    private final List<String> iconTags;
    private String selectedTag;
    private final OnIconClickListener listener;

    public interface OnIconClickListener {
        void onIconClick(String tag);
    }

    public IconSelectionAdapter(List<String> iconTags, String selectedTag, OnIconClickListener listener) {
        this.iconTags = iconTags;
        this.selectedTag = selectedTag;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_icon_selection, parent, false);
        return new IconViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
        String tag = iconTags.get(position);
        holder.bind(tag, tag.equals(selectedTag));
    }

    @Override
    public int getItemCount() {
        return iconTags.size();
    }

    class IconViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        MaterialCardView cardContainer;

        public IconViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.img_selection_icon);
            cardContainer = itemView.findViewById(R.id.card_icon_container);
        }

        public void bind(String tag, boolean isSelected) {
            int resId = itemView.getContext().getResources().getIdentifier(
                    "ic_" + tag, "drawable", itemView.getContext().getPackageName());
            if (resId != 0) {
                imgIcon.setImageResource(resId);
            } else {
                imgIcon.setImageResource(R.drawable.ic_wallet);
            }

            if (isSelected) {
                cardContainer.setStrokeWidth((int) (2 * itemView.getContext().getResources().getDisplayMetrics().density));
                cardContainer.setCardBackgroundColor(itemView.getContext().getResources().getColor(R.color.emerald_green_light));
            } else {
                cardContainer.setStrokeWidth(0);
                cardContainer.setCardBackgroundColor(itemView.getContext().getResources().getColor(R.color.white));
            }

            itemView.setOnClickListener(v -> {
                String oldTag = selectedTag;
                selectedTag = tag;
                notifyItemChanged(iconTags.indexOf(oldTag));
                notifyItemChanged(getAdapterPosition());
                listener.onIconClick(tag);
            });
        }
    }
}
