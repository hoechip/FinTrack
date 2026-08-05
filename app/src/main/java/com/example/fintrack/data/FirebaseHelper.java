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
    private static final String URL_FIREBASE = "https://quynh-e97e6-default-rtdb.asia-southeast1.firebasedatabase.app";
    private static final String NUT_NGAN_SACH = "envelope_budgets";

    private static FirebaseHelper thucThe;
    private DatabaseReference databaseReference;
    private KeHoachNganSach keHoachHienTai;

    public interface LangNgheDuLieu {
        void onDuLieu(KeHoachNganSach keHoach);
        void onError(String loi);
    }

    private FirebaseHelper() {
        FirebaseDatabase database = FirebaseDatabase.getInstance(URL_FIREBASE);
        databaseReference = database.getReference(NUT_NGAN_SACH);
        keHoachHienTai = taoDuLieuMau();
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
        databaseReference.child(thang).setValue(keHoach).addOnCompleteListener(task -> {
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

    public void langNgheKeHoachNganSach(LangNgheDuLieu langNghe) {
        String thang = layThangHienTai();
        databaseReference.child(thang).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    KeHoachNganSach keHoach = snapshot.getValue(KeHoachNganSach.class);
                    if (keHoach != null) {
                        keHoachHienTai = keHoach;
                        if (langNghe != null) {
                            langNghe.onDuLieu(keHoach);
                        }
                        return;
                    }
                } else {
                    // Nếu trên Firebase chưa có dữ liệu, tự động đẩy dữ liệu mẫu lên Firebase ngay
                    databaseReference.child(thang).setValue(keHoachHienTai);
                }
                if (langNghe != null) {
                    langNghe.onDuLieu(keHoachHienTai);
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

    public void dayDuLieuMauLenFirebase(OnCompleteListener<Void> langNghe) {
        String thang = layThangHienTai();
        this.keHoachHienTai = taoDuLieuMau();
        databaseReference.child(thang).setValue(keHoachHienTai).addOnCompleteListener(langNghe);
    }

    public KeHoachNganSach taoDuLieuMau() {
        ArrayList<PhongBi> danhSach = new ArrayList<>();
        // Mock data chuẩn xác theo Ảnh 1 và Ảnh 2
        danhSach.add(new PhongBi("food", "Ăn uống", "food", 5000000L, 1500000L));
        danhSach.add(new PhongBi("rent", "Thuê nhà", "house", 4000000L, 0L));
        danhSach.add(new PhongBi("transport", "Di chuyển", "bus", 1200000L, 0L));
        danhSach.add(new PhongBi("utilities", "Điện nước", "lightning", 2000000L, 0L));
        danhSach.add(new PhongBi("entertainment", "Giải trí", "gamepad", 3000000L, 2250000L));

        return new KeHoachNganSach(layThangHienTai(), 20000000L, danhSach, System.currentTimeMillis());
    }

    public ArrayList<PhongBi> taoDuLieuMauManHinh6() {
        ArrayList<PhongBi> danhSach = new ArrayList<>();
        danhSach.add(new PhongBi("food", "Ăn uống", "food", 5000000L, 1500000L));
        danhSach.add(new PhongBi("entertainment", "Giải trí", "gamepad", 3000000L, 2250000L));
        danhSach.add(new PhongBi("shopping", "Giải trí", "shopping", 3000000L, 2250000L));
        return danhSach;
    }
}
