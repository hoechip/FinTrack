package com.example.fintrack;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.fintrack.data.FirebaseHelper;
import com.example.fintrack.model.KeHoachNganSach;
import com.example.fintrack.model.PhongBi;
import com.example.fintrack.util.DinhDangTien;

import java.util.ArrayList;

public class PhanBoNganSachFragment extends Fragment {

    ImageView btnQuayLai;
    EditText edtTongThuNhap;
    TextView tvTongPhanBo;
    TextView tvSoDuConLai;
    LinearLayout layoutDanhSachPhongBi;
    Button btnLuuNganSach;

    ArrayList<PhongBi> danhSachPhongBi;
    long tongThuNhap = 20000000L;
    boolean dangDinhDangThuNhap = false;

    FirebaseHelper firebaseHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_phan_bo_ngan_sach, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseHelper = FirebaseHelper.layThucThe();
        danhSachPhongBi = new ArrayList<>();

        btnQuayLai = view.findViewById(R.id.btnQuayLai);
        edtTongThuNhap = view.findViewById(R.id.edtTongThuNhap);
        tvTongPhanBo = view.findViewById(R.id.tvTongPhanBo);
        tvSoDuConLai = view.findViewById(R.id.tvSoDuConLai);
        layoutDanhSachPhongBi = view.findViewById(R.id.layoutDanhSachPhongBi);
        btnLuuNganSach = view.findViewById(R.id.btnLuuNganSach);

        btnQuayLai.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showHomeScreen();
            }
        });

        edtTongThuNhap.setText(DinhDangTien.dinhDangVND(tongThuNhap));
        edtTongThuNhap.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (dangDinhDangThuNhap) return;

                long soTien = DinhDangTien.chuyenChuoiSangSo(s.toString());
                tongThuNhap = soTien;

                dangDinhDangThuNhap = true;
                String chuoiDinhDang = DinhDangTien.dinhDangVND(soTien);
                edtTongThuNhap.setText(chuoiDinhDang);
                edtTongThuNhap.setSelection(Math.max(0, chuoiDinhDang.length() - 2));
                dangDinhDangThuNhap = false;

                tinhToanVaCapNhat();
            }
        });

        btnLuuNganSach.setOnClickListener(v -> xuLyLuuNganSach());

        taiDuLieu();
    }

    public void taiDuLieu() {
        KeHoachNganSach keHoachCache = firebaseHelper.layKeHoachHienTai();
        if (keHoachCache != null) {
            hienThiDuLieu(keHoachCache);
        }

        firebaseHelper.langNgheKeHoachNganSach(new FirebaseHelper.LangNgheDuLieu() {
            @Override
            public void onDuLieu(KeHoachNganSach keHoach) {
                if (isAdded() && keHoach != null) {
                    hienThiDuLieu(keHoach);
                }
            }

            @Override
            public void onError(String loi) {
                // Giữ dữ liệu hiển thị hiện tại
            }
        });
    }

    private void hienThiDuLieu(KeHoachNganSach keHoach) {
        if (keHoach.getTongThuNhap() > 0) {
            tongThuNhap = keHoach.getTongThuNhap();
        }

        dangDinhDangThuNhap = true;
        edtTongThuNhap.setText(DinhDangTien.dinhDangVND(tongThuNhap));
        dangDinhDangThuNhap = false;

        danhSachPhongBi.clear();
        if (keHoach.getDanhSachPhongBi() != null) {
            danhSachPhongBi.addAll(keHoach.getDanhSachPhongBi());
        }

        hienThiDanhSachPhongBi();
        tinhToanVaCapNhat();
    }

    private void hienThiDanhSachPhongBi() {
        if (layoutDanhSachPhongBi == null || getContext() == null) return;
        layoutDanhSachPhongBi.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (PhongBi phongBi : danhSachPhongBi) {
            View customView = inflater.inflate(R.layout.item_budget_allocation, layoutDanhSachPhongBi, false);

            ImageView imgCategoryIcon = customView.findViewById(R.id.img_category_icon);
            TextView tvCategoryName = customView.findViewById(R.id.tv_category_name);
            EditText etAllocatedAmount = customView.findViewById(R.id.et_allocated_amount);

            if (phongBi.getTenHangMuc() != null) {
                tvCategoryName.setText(phongBi.getTenHangMuc());
            }

            ganIconChoHangMuc(imgCategoryIcon, phongBi.getLoaiIcon());
            etAllocatedAmount.setText(DinhDangTien.dinhDangVND(phongBi.getHanMuc()));

            etAllocatedAmount.addTextChangedListener(new TextWatcher() {
                private boolean isFormatting = false;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (isFormatting) return;

                    long parsed = DinhDangTien.chuyenChuoiSangSo(s.toString());
                    phongBi.setHanMuc(parsed);

                    isFormatting = true;
                    String formatted = DinhDangTien.dinhDangVND(parsed);
                    etAllocatedAmount.setText(formatted);
                    etAllocatedAmount.setSelection(Math.max(0, formatted.length() - 2));
                    isFormatting = false;

                    tinhToanVaCapNhat();
                }
            });

            layoutDanhSachPhongBi.addView(customView);
        }
    }

    private void tinhToanVaCapNhat() {
        if (tvTongPhanBo == null || tvSoDuConLai == null || getContext() == null) return;

        long tongPhanBo = 0;
        for (PhongBi pb : danhSachPhongBi) {
            tongPhanBo += pb.getHanMuc();
        }

        long soDuConLai = tongThuNhap - tongPhanBo;

        tvTongPhanBo.setText(DinhDangTien.dinhDangVND(tongPhanBo));
        tvSoDuConLai.setText(DinhDangTien.dinhDangVNDCoDau(soDuConLai));

        int mauXanh = ContextCompat.getColor(requireContext(), R.color.soft_green);
        int mauDo = ContextCompat.getColor(requireContext(), R.color.soft_red);
        int mauLucBao = ContextCompat.getColor(requireContext(), R.color.emerald_green);
        int mauKhoa = ContextCompat.getColor(requireContext(), R.color.btn_disabled_bg);
        int mauChuKhoa = ContextCompat.getColor(requireContext(), R.color.btn_disabled_text);
        int mauTrang = ContextCompat.getColor(requireContext(), R.color.white);

        if (soDuConLai >= 0) {
            tvSoDuConLai.setTextColor(mauXanh);
            btnLuuNganSach.setEnabled(true);
            btnLuuNganSach.setBackgroundTintList(ColorStateList.valueOf(mauLucBao));
            btnLuuNganSach.setTextColor(mauTrang);
        } else {
            tvSoDuConLai.setTextColor(mauDo);
            btnLuuNganSach.setEnabled(false);
            btnLuuNganSach.setBackgroundTintList(ColorStateList.valueOf(mauKhoa));
            btnLuuNganSach.setTextColor(mauChuKhoa);
        }
    }

    private void xuLyLuuNganSach() {
        long tongPhanBo = 0;
        for (PhongBi pb : danhSachPhongBi) {
            tongPhanBo += pb.getHanMuc();
        }

        long soDuConLai = tongThuNhap - tongPhanBo;
        if (soDuConLai < 0) {
            Toast.makeText(requireContext(), "Tổng phân bổ vượt quá thu nhập! Vui lòng điều chỉnh lại.", Toast.LENGTH_LONG).show();
            return;
        }

        hienThiDialogXacNhanLuu(tongPhanBo, soDuConLai);
    }

    private void hienThiDialogXacNhanLuu(long tongPhanBo, long soDuConLai) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Xác nhận lưu ngân sách");
        builder.setMessage("Bạn có chắc chắn muốn lưu kế hoạch phân bổ ngân sách tháng "
                + firebaseHelper.layThangHienTai()
                + " lên Firebase không?\n\n"
                + "• Tổng thu nhập: " + DinhDangTien.dinhDangVND(tongThuNhap) + "\n"
                + "• Tổng phân bổ: " + DinhDangTien.dinhDangVND(tongPhanBo) + "\n"
                + "• Số dư còn lại: " + DinhDangTien.dinhDangVNDCoDau(soDuConLai));

        builder.setPositiveButton("Đồng ý", (dialog, which) -> {
            Toast.makeText(requireContext(), "Lưu ngân sách thành công!", Toast.LENGTH_SHORT).show();
            thucHienLuuLenFirebase();
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void thucHienLuuLenFirebase() {
        KeHoachNganSach keHoach = new KeHoachNganSach(
                firebaseHelper.layThangHienTai(),
                tongThuNhap,
                danhSachPhongBi,
                System.currentTimeMillis()
        );

        firebaseHelper.luuKeHoachNganSach(keHoach, task -> {
            if (task.isSuccessful()) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Đã đồng bộ lên Firebase thành công!", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (isAdded()) {
                    String loi = task.getException() != null ? task.getException().getMessage() : "Lỗi lưu dữ liệu";
                    Toast.makeText(requireContext(), "Lỗi: " + loi, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void ganIconChoHangMuc(ImageView imageView, String loaiIcon) {
        if (loaiIcon == null) {
            imageView.setImageResource(R.drawable.ic_wallet);
            return;
        }
        switch (loaiIcon) {
            case "food":
                imageView.setImageResource(R.drawable.ic_food);
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
