package com.example.fintrack.data;

import android.util.Log;
import androidx.annotation.NonNull;

import com.example.fintrack.model.KeHoachNganSach;
import com.example.fintrack.model.PhongBi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";
    private static final String URL_FIREBASE = "https://fintrackhue-default-rtdb.firebaseio.com";
    private static final String NUT_NGAN_SACH = "envelope_budgets";

    private static FirebaseHelper thucThe;
    private DatabaseReference baseReference;
    private KeHoachNganSach keHoachHienTai;

    public interface LangNgheDuLieu {
        void onDuLieu(KeHoachNganSach keHoach);
        void onError(String loi);
    }

    private FirebaseHelper() {
        FirebaseDatabase database = FirebaseDatabase.getInstance(URL_FIREBASE);
        baseReference = database.getReference(); // Reference gốc
        keHoachHienTai = taoDuLieuMau();
    }

    private DatabaseReference getBudgetReference() {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        String uid = (user != null) ? user.getUid() : "guest";
        return baseReference.child("users").child(uid).child(NUT_NGAN_SACH);
    }

    public static synchronized FirebaseHelper layThucThe() {
        if (thucThe == null) {
            thucThe = new FirebaseHelper();
        }
        return thucThe;
    }

    public String layThangHienTai() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        return sdf.format(new Date());
    }

    public KeHoachNganSach layKeHoachHienTai() {
        return keHoachHienTai;
    }

    public void luuKeHoachNganSach(KeHoachNganSach keHoach, OnCompleteListener<Void> langNghe) {
        this.keHoachHienTai = keHoach;
        String thang = keHoach.getThang() != null ? keHoach.getThang() : layThangHienTai();
        Log.d(TAG, "Đang lưu ngân sách lên Firebase tại node: " + NUT_NGAN_SACH + "/" + thang);
        getBudgetReference().child(thang).setValue(keHoach).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Lưu thành công lên Firebase!");
            } else {
                Log.e(TAG, "Lưu thất bại: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
            }
            if (langNghe != null) {
                langNghe.onComplete(task);
            }
        });
    }

    public void langNgheKeHoachNganSach(String thang, LangNgheDuLieu langNghe) {
        String thangTruyVan = (thang != null) ? thang : layThangHienTai();
        getBudgetReference().child(thangTruyVan).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    KeHoachNganSach keHoach = snapshot.getValue(KeHoachNganSach.class);
                    if (keHoach != null) {
                        // Chỉ cập nhật keHoachHienTai nếu là tháng hiện tại
                        if (thangTruyVan.equals(layThangHienTai())) {
                            keHoachHienTai = keHoach;
                        }
                        if (langNghe != null) {
                            langNghe.onDuLieu(keHoach);
                        }
                        return;
                    }
                } else if (thangTruyVan.equals(layThangHienTai())) {
                    // Nếu là tháng hiện tại và chưa có dữ liệu, tự động đẩy dữ liệu mẫu
                    getBudgetReference().child(thangTruyVan).setValue(keHoachHienTai);
                }
                
                if (langNghe != null) {
                    // Nếu không có dữ liệu cho tháng được chọn (và không phải tháng hiện tại), trả về null hoặc object trống
                    langNghe.onDuLieu(snapshot.exists() ? null : (thangTruyVan.equals(layThangHienTai()) ? keHoachHienTai : null));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (langNghe != null) {
                    langNghe.onError(error.getMessage());
                }
            }
        });
    }

    public void langNgheKeHoachNganSach(LangNgheDuLieu langNghe) {
        langNgheKeHoachNganSach(null, langNghe);
    }

    public void dayDuLieuMauLenFirebase(OnCompleteListener<Void> langNghe) {
        String thang = layThangHienTai();
        this.keHoachHienTai = taoDuLieuMau();
        getBudgetReference().child(thang).setValue(keHoachHienTai).addOnCompleteListener(langNghe);
    }

    public KeHoachNganSach taoDuLieuMau() {
        // Trả về kế hoạch trống cho tài khoản mới
        return new KeHoachNganSach(layThangHienTai(), 0L, new ArrayList<>(), System.currentTimeMillis());
    }

    public ArrayList<PhongBi> taoDuLieuMauManHinh6() {
        ArrayList<PhongBi> danhSach = new ArrayList<>();
        danhSach.add(new PhongBi("food", "Ăn uống", "food", 5000000L, 1500000L));
        danhSach.add(new PhongBi("entertainment", "Giải trí", "gamepad", 3000000L, 2250000L));
        danhSach.add(new PhongBi("shopping", "Giải trí", "shopping", 3000000L, 2250000L));
        return danhSach;
    }
}
