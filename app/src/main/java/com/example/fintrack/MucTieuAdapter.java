package com.example.fintrack;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MucTieuAdapter
        extends RecyclerView.Adapter<MucTieuAdapter.ViewHolder> {

    private final List<MucTieuTietKiem> danhSach;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MucTieuTietKiem mucTieu);

        void onSuaClick(MucTieuTietKiem mucTieu);

        void onXoaClick(MucTieuTietKiem mucTieu);
    }

    public MucTieuAdapter(List<MucTieuTietKiem> danhSach,
                          OnItemClickListener listener) {
        this.danhSach = danhSach;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_muctieutietkiem,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        MucTieuTietKiem mucTieu = danhSach.get(position);

        NumberFormat formatter =
                NumberFormat.getNumberInstance(
                        new Locale("vi", "VN")
                );

        holder.txt_m9_ten.setText(
                mucTieu.getTenMucTieu()
        );

        holder.txt_m9_sotien.setText(
                formatter.format(mucTieu.getSoTienHienTai())
                        + " đ / "
                        + formatter.format(mucTieu.getSoTienMucTieu())
                        + " đ"
        );

        holder.txt_m9_hanchot.setText(
                "Hạn chót: " + mucTieu.getNgayHanChot()
        );

        int phanTram = mucTieu.tinhPhanTram();

        holder.progress_m9_tiendo.setProgress(phanTram);
        holder.txt_m9_phantram.setText(phanTram + "%");

        holder.card_m9_item.setOnClickListener(
                view -> listener.onItemClick(mucTieu)
        );

        holder.btn_m9_sua.setOnClickListener(
                view -> listener.onSuaClick(mucTieu)
        );

        holder.btn_m9_xoa.setOnClickListener(
                view -> listener.onXoaClick(mucTieu)
        );
    }

    @Override
    public int getItemCount() {
        return danhSach.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        CardView card_m9_item;
        TextView txt_m9_ten;
        TextView txt_m9_sotien;
        TextView txt_m9_phantram;
        TextView txt_m9_hanchot;
        ProgressBar progress_m9_tiendo;
        Button btn_m9_sua;
        Button btn_m9_xoa;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            card_m9_item =
                    itemView.findViewById(R.id.card_m9_item);

            txt_m9_ten =
                    itemView.findViewById(R.id.txt_m9_ten);

            txt_m9_sotien =
                    itemView.findViewById(R.id.txt_m9_sotien);

            txt_m9_phantram =
                    itemView.findViewById(R.id.txt_m9_phantram);

            txt_m9_hanchot =
                    itemView.findViewById(R.id.txt_m9_hanchot);

            progress_m9_tiendo =
                    itemView.findViewById(R.id.progress_m9_tiendo);

            btn_m9_sua =
                    itemView.findViewById(R.id.btn_m9_sua);

            btn_m9_xoa =
                    itemView.findViewById(R.id.btn_m9_xoa);
        }
    }
}