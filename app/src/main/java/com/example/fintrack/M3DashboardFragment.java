package com.example.fintrack;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fintrack.data.FirebaseHelper;
import com.example.fintrack.databinding.FragmentFirstBinding;
import com.example.fintrack.model.KeHoachNganSach;
import com.example.fintrack.model.PhongBi;
import com.example.fintrack.util.DinhDangTien;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class M3DashboardFragment extends Fragment {

    private static final String TAG = "M3DashboardFragment";
    private FragmentFirstBinding binding;
    private DatabaseHelper dbHelper;
    private FirebaseHelper firebaseHelper;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());
        firebaseHelper = FirebaseHelper.layThucThe();

        if (binding.rvM3RecentTransactions != null) {
            binding.rvM3RecentTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.rvM3RecentTransactions.setHasFixedSize(true);
        }

        loadRecentTransactions();
        listenToBudgetChanges();
        loadSavingsGoals();

        binding.btnM3ViewAllProgress.setOnClickListener(v -> {
            NavHostFragment.findNavController(M3DashboardFragment.this)
                    .navigate(R.id.fragment_budget_progress);
        });

        binding.btnM3ViewAllSavings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), M9DanhSachMucTieuActivity.class);
            startActivity(intent);
        });

        binding.btnM3Menu.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });

        binding.btnM3ViewHistory.setOnClickListener(v -> {
            NavHostFragment.findNavController(M3DashboardFragment.this)
                    .navigate(R.id.fragment_history);
        });
        
        binding.imgM3Avatar.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecentTransactions();
        loadAvatar();
    }

    private void loadAvatar() {
        if (binding == null) return;
        
        android.content.SharedPreferences sp = requireContext().getSharedPreferences("FinTrack", android.content.Context.MODE_PRIVATE);
        String avatarPath = sp.getString("AVATAR_PATH", "");

        if (!avatarPath.isEmpty()) {
            java.io.File file = new java.io.File(avatarPath);
            if (file.exists()) {
                binding.imgM3Avatar.setImageURI(null);
                binding.imgM3Avatar.setImageURI(android.net.Uri.fromFile(file));
            } else {
                binding.imgM3Avatar.setImageResource(R.drawable.ic_user_placeholder);
            }
        } else {
            binding.imgM3Avatar.setImageResource(R.drawable.ic_user_placeholder);
        }
    }

    private void listenToBudgetChanges() {
        firebaseHelper.langNgheKeHoachNganSach(new FirebaseHelper.LangNgheDuLieu() {
            @Override
            public void onDuLieu(KeHoachNganSach keHoach) {
                if (isAdded() && binding != null && keHoach != null) {
                    updateDashboardStats(keHoach);
                }
            }

            @Override
            public void onError(String loi) {
                Log.e(TAG, "Lỗi lắng nghe ngân sách: " + loi);
            }
        });
    }

    private void updateDashboardStats(KeHoachNganSach keHoach) {
        long tongThuNhap = keHoach.getTongThuNhap();
        long tongDaTieu = 0;

        if (keHoach.getDanhSachPhongBi() != null) {
            for (PhongBi pb : keHoach.getDanhSachPhongBi()) {
                tongDaTieu += pb.getDaTieu();
            }
        }

        long soDuHienTai = tongThuNhap - tongDaTieu;

        binding.txtM3TotalBalance.setText(DinhDangTien.dinhDangVND(soDuHienTai));
        binding.txtM3TotalExpense.setText(DinhDangTien.dinhDangVND(tongDaTieu));

        // Cập nhật biểu đồ tiến độ chi tiêu động
        updateSpendingProgressGrid(keHoach);
    }

    private void updateSpendingProgressGrid(KeHoachNganSach keHoach) {
        if (binding.glM3SpendingList == null || keHoach.getDanhSachPhongBi() == null) return;

        binding.glM3SpendingList.removeAllViews();
        ArrayList<PhongBi> list = keHoach.getDanhSachPhongBi();
        
        // Hiển thị tối đa 4 hạng mục trên Dashboard
        int limit = Math.min(list.size(), 4);
        for (int i = 0; i < limit; i++) {
            PhongBi pb = list.get(i);
            View item = createSpendingProgressItem(pb);
            binding.glM3SpendingList.addView(item);
        }
    }

    private View createSpendingProgressItem(PhongBi pb) {
        LinearLayout container = new LinearLayout(getContext());
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(8, 8, 8, 8);
        container.setLayoutParams(params);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(4, 4, 4, 4);

        // Header: Tên và Icon
        LinearLayout header = new LinearLayout(getContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        header.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ImageView cho icon màu sắc
        ImageView imgIcon = new ImageView(getContext());
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams((int) (20 * getResources().getDisplayMetrics().density), (int) (20 * getResources().getDisplayMetrics().density));
        imgParams.rightMargin = (int) (6 * getResources().getDisplayMetrics().density);
        imgIcon.setLayoutParams(imgParams);
        
        String loaiIcon = pb.getLoaiIcon();
        int resId = getResources().getIdentifier("ic_" + (loaiIcon != null ? loaiIcon : "wallet"), "drawable", requireContext().getPackageName());
        if (resId != 0) {
            imgIcon.setImageResource(resId);
        } else {
            imgIcon.setImageResource(R.drawable.ic_wallet);
        }
        header.addView(imgIcon);

        TextView tvName = new TextView(getContext());
        tvName.setText(pb.getTenHangMuc());
        tvName.setTextSize(13);
        tvName.setTextColor(getResources().getColor(R.color.text_dark));
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        tvName.setSingleLine(true);
        tvName.setEllipsize(android.text.TextUtils.TruncateAt.END);
        header.addView(tvName);

        container.addView(header);

        // Progress Bar
        ProgressBar progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (6 * getResources().getDisplayMetrics().density));
        pbParams.topMargin = (int) (8 * getResources().getDisplayMetrics().density);
        progressBar.setLayoutParams(pbParams);
        
        int percent = pb.getPhanTramDaTieu();
        progressBar.setMax(100);
        progressBar.setProgress(Math.min(percent, 100));
        
        // Màu sắc theo mức độ chi tiêu
        if (percent >= 100) {
            progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.soft_red)));
        } else if (percent >= 80) {
            progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.amber_warning)));
        } else {
            progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.emerald_green)));
        }
        
        container.addView(progressBar);

        return container;
    }

    private void updateSmallProgressBars(KeHoachNganSach keHoach) {
        // Method replaced by updateSpendingProgressGrid
    }

    private void loadSavingsGoals() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FirestoreConst.COLLECTION_MUC_TIEU)
                .limit(3)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || binding == null) return;
                    
                    binding.llM3SavingsList.removeAllViews();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                        MucTieuTietKiem mt = doc.toObject(MucTieuTietKiem.class);
                        if (mt != null) {
                            addSavingsGoalView(mt);
                        }
                    }
                });
    }

    private void addSavingsGoalView(MucTieuTietKiem mt) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_budget_progress, binding.llM3SavingsList, false);
        
        TextView tvName = view.findViewById(R.id.tv_category_name);
        TextView tvPercent = view.findViewById(R.id.tv_percentage);
        ProgressBar pb = view.findViewById(R.id.progress_bar_spending);
        TextView tvAllocated = view.findViewById(R.id.tv_allocated_amount);
        TextView tvSpent = view.findViewById(R.id.tv_spent_amount);
        TextView tvStatus = view.findViewById(R.id.tv_remaining_status);
        
        tvName.setText(mt.getTenMucTieu());
        int percent = mt.tinhPhanTram();
        tvPercent.setText(percent + "%");
        pb.setProgress(percent);
        
        tvAllocated.setText("Mục tiêu: " + DinhDangTien.dinhDangVND(mt.getSoTienMucTieu()));
        tvSpent.setText("Đã có: " + DinhDangTien.dinhDangVND(mt.getSoTienHienTai()));
        
        long conLai = mt.getSoTienMucTieu() - mt.getSoTienHienTai();
        tvStatus.setText("Còn thiếu: " + DinhDangTien.dinhDangVND(Math.max(0, conLai)));
        
        view.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChiTietMucTieuActivity.class);
            intent.putExtra("id", mt.getId() != null ? mt.getId() : "temp_id"); 
            // Note: In real app, make sure ID is populated in mt
            startActivity(intent);
        });
        
        binding.llM3SavingsList.addView(view);
    }

    private void loadRecentTransactions() {
        if (binding == null) return;

        List<Transaction> transactionList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT t.*, e." + DatabaseHelper.COLUMN_ENV_NAME + 
                       ", e." + DatabaseHelper.COLUMN_ENV_ICON + 
                       " FROM " + DatabaseHelper.TABLE_TRANSACTIONS + " t" +
                       " LEFT JOIN " + DatabaseHelper.TABLE_ENVELOPES + " e" +
                       " ON t." + DatabaseHelper.COLUMN_TRANS_ENV_ID + " = e." + DatabaseHelper.COLUMN_ENV_ID +
                       " ORDER BY t." + DatabaseHelper.COLUMN_TRANS_DATE + " DESC LIMIT 5";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_TITLE));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_AMOUNT));
                String rawDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_DATE));
                // Lấy tag icon từ bảng phong bì thay vì để trống
                String catIcon = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ENV_ICON));

                String formattedDate = formatDisplayDate(rawDate);
                String amountStr = "-" + formatter.format(amount) + " đ";
                
                // Truyền catIcon vào constructor của Transaction
                transactionList.add(new Transaction(title, formattedDate, amountStr, catIcon));
            } while (cursor.moveToNext());
        }
        cursor.close();

        RecentTransactionAdapter adapter = new RecentTransactionAdapter(transactionList);
        binding.rvM3RecentTransactions.setAdapter(adapter);
    }

    private String formatDisplayDate(String rawDate) {
        try {
            SimpleDateFormat dbSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = dbSdf.parse(rawDate);
            if (date == null) return rawDate;

            Calendar now = Calendar.getInstance();
            Calendar target = Calendar.getInstance();
            target.setTime(date);

            SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String timeStr = timeSdf.format(date);

            if (now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)) {
                return "Hôm nay • " + timeStr;
            }

            now.add(Calendar.DAY_OF_YEAR, -1);
            if (now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)) {
                return "Hôm qua • " + timeStr;
            }

            SimpleDateFormat displaySdf = new SimpleDateFormat("dd/MM • HH:mm", Locale.getDefault());
            return displaySdf.format(date);
        } catch (Exception e) {
            return rawDate;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
