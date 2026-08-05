package com.example.fintrack;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.fintrack.data.FirebaseHelper;
import com.example.fintrack.model.KeHoachNganSach;
import com.example.fintrack.model.PhongBi;
import com.example.fintrack.util.DinhDangTien;

import java.util.ArrayList;

public class TienDoNganSachFragment extends Fragment {

    ImageView btnQuayLai;
    TextView tvTieuDe;
    ImageView ivAnhBia;
    LinearLayout layoutDanhSachPhongBi;

    ArrayList<PhongBi> danhSachPhongBi;
    FirebaseHelper firebaseHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tien_do_ngan_sach, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseHelper = FirebaseHelper.layThucThe();
        danhSachPhongBi = new ArrayList<>();

        btnQuayLai = view.findViewById(R.id.btnQuayLai);
        tvTieuDe = view.findViewById(R.id.tvTieuDe);
        ivAnhBia = view.findViewById(R.id.ivAnhBia);
        layoutDanhSachPhongBi = view.findViewById(R.id.layoutDanhSachPhongBi);

        btnQuayLai.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showHomeScreen();
            }
        });

        taiDuLieu();
    }

    public void taiDuLieu() {
        KeHoachNganSach keHoachCache = firebaseHelper.layKeHoachHienTai();
        if (keHoachCache != null && keHoachCache.getDanhSachPhongBi() != null && !keHoachCache.getDanhSachPhongBi().isEmpty()) {
            danhSachPhongBi.clear();
            danhSachPhongBi.addAll(keHoachCache.getDanhSachPhongBi());
        } else {
            danhSachPhongBi.clear();
            danhSachPhongBi.addAll(firebaseHelper.taoDuLieuMauManHinh6());
        }
        hienThiDanhSach();

        firebaseHelper.langNgheKeHoachNganSach(new FirebaseHelper.LangNgheDuLieu() {
            @Override
            public void onDuLieu(KeHoachNganSach keHoach) {
                if (isAdded() && keHoach != null && keHoach.getDanhSachPhongBi() != null) {
                    danhSachPhongBi.clear();
                    danhSachPhongBi.addAll(keHoach.getDanhSachPhongBi());
                    hienThiDanhSach();
                }
            }

            @Override
            public void onError(String loi) {
                // Giữ dữ liệu hiển thị hiện tại
            }
        });
    }

    private void hienThiDanhSach() {
        if (layoutDanhSachPhongBi == null || getContext() == null) return;
        layoutDanhSachPhongBi.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (PhongBi phongBi : danhSachPhongBi) {
            View customView = inflater.inflate(R.layout.item_budget_progress, layoutDanhSachPhongBi, false);

            ImageView imgCategoryIcon = customView.findViewById(R.id.img_category_icon);
            TextView tvCategoryName = customView.findViewById(R.id.tv_category_name);
            TextView tvPercentage = customView.findViewById(R.id.tv_percentage);
            ProgressBar progressBarSpending = customView.findViewById(R.id.progress_bar_spending);
            TextView tvAllocatedAmount = customView.findViewById(R.id.tv_allocated_amount);
            TextView tvSpentAmount = customView.findViewById(R.id.tv_spent_amount);
            TextView tvRemainingStatus = customView.findViewById(R.id.tv_remaining_status);

            if (phongBi.getTenHangMuc() != null) {
                tvCategoryName.setText(phongBi.getTenHangMuc());
            }

            ganIconChoHangMuc(imgCategoryIcon, phongBi.getLoaiIcon());

            int phanTram = phongBi.getPhanTramDaTieu();
            tvPercentage.setText(phanTram + "%");
            progressBarSpending.setProgress(Math.min(100, phanTram));

            tvAllocatedAmount.setText("Hạn mức: " + DinhDangTien.dinhDangVND(phongBi.getHanMuc()));
            tvSpentAmount.setText("Đã tiêu: " + DinhDangTien.dinhDangVND(phongBi.getDaTieu()));

            long conLai = phongBi.getSoDuConLai();

            int emeraldColor = ContextCompat.getColor(requireContext(), R.color.emerald_green);
            int amberColor = ContextCompat.getColor(requireContext(), R.color.amber_warning);
            int redColor = ContextCompat.getColor(requireContext(), R.color.soft_red);

            if (phanTram < 70) {
                // An toàn (< 70%)
                Drawable progressDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.progress_emerald);
                progressBarSpending.setProgressDrawable(progressDrawable);
                tvRemainingStatus.setText("Còn lại: " + DinhDangTien.dinhDangVNDCoDau(conLai) + " (An toàn)");
                tvRemainingStatus.setTextColor(emeraldColor);
            } else if (phanTram < 100) {
                // Cảnh báo (70% - 99%)
                Drawable progressDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.progress_amber);
                progressBarSpending.setProgressDrawable(progressDrawable);
                tvRemainingStatus.setText("Còn lại: " + DinhDangTien.dinhDangVNDCoDau(conLai) + " (Cảnh báo)");
                tvRemainingStatus.setTextColor(amberColor);
            } else {
                // Vượt hạn mức (>= 100%)
                Drawable progressDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.progress_red);
                progressBarSpending.setProgressDrawable(progressDrawable);
                tvRemainingStatus.setText("Vượt: " + DinhDangTien.dinhDangVNDCoDau(Math.abs(conLai)) + " (Vượt hạn mức)");
                tvRemainingStatus.setTextColor(redColor);
            }

            layoutDanhSachPhongBi.addView(customView);
        }
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
}
