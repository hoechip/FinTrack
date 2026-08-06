package com.example.fintrack.adapter;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.fintrack.R;
import com.example.fintrack.model.PhongBi;
import com.example.fintrack.util.DinhDangTien;

import java.util.ArrayList;

public class PhongBiTienDoAdapter extends ArrayAdapter<PhongBi> {

    Activity context;
    int resource;
    ArrayList<PhongBi> listPhongBi;

    public PhongBiTienDoAdapter(Activity context, int resource, ArrayList<PhongBi> listPhongBi) {
        super(context, resource, listPhongBi);
        this.context = context;
        this.resource = resource;
        this.listPhongBi = listPhongBi;
    }

    @Override
    public int getCount() {
        return this.listPhongBi != null ? this.listPhongBi.size() : 0;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View customView = layoutInflater.inflate(resource, null);

        ImageView imgCategoryIcon = customView.findViewById(R.id.img_category_icon);
        TextView tvCategoryName = customView.findViewById(R.id.tv_category_name);
        TextView tvPercentage = customView.findViewById(R.id.tv_percentage);
        ProgressBar progressBarSpending = customView.findViewById(R.id.progress_bar_spending);
        TextView tvAllocatedAmount = customView.findViewById(R.id.tv_allocated_amount);
        TextView tvSpentAmount = customView.findViewById(R.id.tv_spent_amount);
        TextView tvRemainingStatus = customView.findViewById(R.id.tv_remaining_status);

        PhongBi phongBi = listPhongBi.get(position);

        if (phongBi.getTenHangMuc() != null) {
            tvCategoryName.setText(phongBi.getTenHangMuc());
        } else {
            tvCategoryName.setText("");
        }

        ganIconChoHangMuc(imgCategoryIcon, phongBi.getLoaiIcon());

        int phanTram = phongBi.getPhanTramDaTieu();
        tvPercentage.setText(phanTram + "%");
        progressBarSpending.setProgress(Math.min(100, phanTram));

        tvAllocatedAmount.setText("Hạn mức: " + DinhDangTien.dinhDangVND(phongBi.getHanMuc()));
        tvSpentAmount.setText("Đã tiêu: " + DinhDangTien.dinhDangVND(phongBi.getDaTieu()));

        long conLai = phongBi.getSoDuConLai();

        int emeraldColor = ContextCompat.getColor(context, R.color.emerald_green);
        int amberColor = ContextCompat.getColor(context, R.color.amber_warning);
        int redColor = ContextCompat.getColor(context, R.color.soft_red);

        if (phanTram < 70) {
            // An toàn
            Drawable progressDrawable = ContextCompat.getDrawable(context, R.drawable.progress_emerald);
            progressBarSpending.setProgressDrawable(progressDrawable);
            tvRemainingStatus.setText("Còn lại: " + DinhDangTien.dinhDangVNDCoDau(conLai) + " (An toàn)");
            tvRemainingStatus.setTextColor(emeraldColor);
        } else if (phanTram < 100) {
            // Cảnh báo
            Drawable progressDrawable = ContextCompat.getDrawable(context, R.drawable.progress_amber);
            progressBarSpending.setProgressDrawable(progressDrawable);
            tvRemainingStatus.setText("Còn lại: " + DinhDangTien.dinhDangVNDCoDau(conLai) + " (Cảnh báo)");
            tvRemainingStatus.setTextColor(amberColor);
        } else {
            // Vượt hạn mức
            Drawable progressDrawable = ContextCompat.getDrawable(context, R.drawable.progress_red);
            progressBarSpending.setProgressDrawable(progressDrawable);
            tvRemainingStatus.setText("Vượt: " + DinhDangTien.dinhDangVNDCoDau(Math.abs(conLai)) + " (Vượt hạn mức)");
            tvRemainingStatus.setTextColor(redColor);
        }

        return customView;
    }

    private void ganIconChoHangMuc(ImageView imageView, String loaiIcon) {
        if (loaiIcon == null) {
            imageView.setImageResource(R.drawable.ic_wallet);
            return;
        }
        switch (loaiIcon) {
            case "food":
                imageView.setImageResource(R.drawable.ic_fork_knife);
                break;
            case "house":
                imageView.setImageResource(R.drawable.ic_home);
                break;
            case "bus":
                imageView.setImageResource(R.drawable.ic_car);
                break;
            case "lightning":
                imageView.setImageResource(R.drawable.ic_bulb);
                break;
            case "gamepad":
                imageView.setImageResource(R.drawable.ic_gamepad);
                break;
            case "shopping":
                imageView.setImageResource(R.drawable.ic_shopping);
                break;
            default:
                imageView.setImageResource(R.drawable.ic_wallet);
                break;
        }
    }

    public void setListPhongBi(ArrayList<PhongBi> danhSachMoi) {
        this.listPhongBi.clear();
        if (danhSachMoi != null) {
            this.listPhongBi.addAll(danhSachMoi);
        }
        notifyDataSetChanged();
    }
}
