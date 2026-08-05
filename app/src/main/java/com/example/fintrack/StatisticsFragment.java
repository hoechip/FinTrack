package com.example.fintrack;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.fintrack.data.FirebaseHelper;
import com.example.fintrack.databinding.FragmentStatisticsBinding;
import com.example.fintrack.model.KeHoachNganSach;
import com.example.fintrack.model.PhongBi;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StatisticsFragment extends Fragment {

    private static final String TAG = "StatisticsFragment";
    private FragmentStatisticsBinding binding;
    private FirebaseFirestore db;
    private FirebaseHelper firebaseHelper;
    private EnvelopeAdapter adapter;
    private List<Envelope> envelopeList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            binding = FragmentStatisticsBinding.inflate(inflater, container, false);
            db = FirebaseFirestore.getInstance();
            return binding.getRoot();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView", e);
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    private String currentSelectedMonth;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (binding == null) return;

        try {
            firebaseHelper = FirebaseHelper.layThucThe();
            currentSelectedMonth = firebaseHelper.layThangHienTai();
            setupRecyclerView();
            setupDatePicker();
            loadMonthData(currentSelectedMonth);
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
            Toast.makeText(getContext(), "Error loading statistics", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMonthData(String month) {
        firebaseHelper.langNgheKeHoachNganSach(month, new FirebaseHelper.LangNgheDuLieu() {
            @Override
            public void onDuLieu(KeHoachNganSach keHoach) {
                if (isAdded() && binding != null) {
                    if (keHoach != null) {
                        processBudgetData(keHoach);
                    } else {
                        // Trường hợp tháng không có dữ liệu
                        binding.txtM5Balance.setText("đ 0");
                        binding.pbM5Income.setProgress(0);
                        binding.pbM5Expense.setProgress(0);
                        envelopeList.clear();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "Không có dữ liệu cho tháng: " + month, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onError(String loi) {
                Log.e(TAG, "Lỗi nạp dữ liệu tháng " + month + ": " + loi);
            }
        });
    }

    private void processBudgetData(KeHoachNganSach keHoach) {
        long tongThuNhap = keHoach.getTongThuNhap();
        long tongDaTieu = 0;
        
        ArrayList<PhongBi> phongBis = keHoach.getDanhSachPhongBi();
        if (phongBis != null) {
            // Sắp xếp phong bì theo số tiền đã tiêu giảm dần
            Collections.sort(phongBis, (p1, p2) -> Long.compare(p2.getDaTieu(), p1.getDaTieu()));
            
            envelopeList.clear();
            for (PhongBi pb : phongBis) {
                tongDaTieu += pb.getDaTieu();
                envelopeList.add(chuyenPhongBiSangEnvelope(pb));
            }
            adapter.notifyDataSetChanged();
        }

        updateUI(tongThuNhap, tongDaTieu);
    }

    private Envelope chuyenPhongBiSangEnvelope(PhongBi pb) {
        // Tái sử dụng logic ánh xạ icon từ các fragment khác
        int iconRes = R.drawable.ic_wallet;
        String loaiIcon = pb.getLoaiIcon();
        if (loaiIcon != null) {
            int resId = getResources().getIdentifier("ic_" + loaiIcon, "drawable", requireContext().getPackageName());
            if (resId != 0) iconRes = resId;
        }
        
        return new Envelope(pb.getId(), pb.getTenHangMuc(), (double)pb.getDaTieu(), (double)pb.getHanMuc(), pb.getLoaiIcon(), null, iconRes);
    }

    private void setupRecyclerView() {
        envelopeList = new ArrayList<>();
        adapter = new EnvelopeAdapter(envelopeList);
        binding.rvM5Envelopes.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvM5Envelopes.setAdapter(adapter);
    }

    private void setupDatePicker() {
        if (binding.btnM5DatePicker == null) return;
        binding.btnM5DatePicker.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn tháng")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            
            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(selection);
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                String selectedMonth = sdf.format(calendar.getTime());
                
                if (!selectedMonth.equals(currentSelectedMonth)) {
                    currentSelectedMonth = selectedMonth;
                    loadMonthData(currentSelectedMonth);
                }
            });

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });
    }

    private void updateUI(double income, double expense) {
        if (binding == null) return;

        double balance = income - expense;
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        binding.txtM5Balance.setText(currencyFormat.format(balance));

        if (income > 0) {
            // Tính toán tỷ lệ phần trăm dựa trên Tổng thu nhập (Bể tiền)
            int expensePercent = (int) ((expense / income) * 100);
            int incomePercent = 100 - expensePercent;

            binding.pbM5Income.setProgress(Math.max(0, incomePercent));
            binding.pbM5Expense.setProgress(Math.min(100, expensePercent));
        } else {
            binding.pbM5Income.setProgress(0);
            binding.pbM5Expense.setProgress(0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
