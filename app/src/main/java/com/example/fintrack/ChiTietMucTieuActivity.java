package com.example.fintrack;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Transaction;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * M10 - Chi tiết mục tiêu & Bỏ ống tiết kiệm thủ công.
 *
 * Luồng chính (theo mục 3.10 tài liệu giao diện):
 *  1. Người dùng gõ số tiền mặt vừa cất riêng đi ngoài đời (Dialog nhập liệu).
 *  2. Hệ thống chạy 1 Firestore Transaction để đảm bảo tính toàn vẹn dữ liệu:
 *       - Cộng dồn số tiền đó vào trường soTienHienTai của mục tiêu.
 *       - Tự tạo 1 bản ghi giao dịch mới trong collection "GiaoDich" với nội
 *         dung "Gửi quỹ tiết kiệm" để Dashboard (M3) trừ số dư khả dụng.
 *  3. Nếu tiến độ đạt 100%, hiển thị thông báo chúc mừng rồi điều hướng về
 *     Dashboard.
 */
public class ChiTietMucTieuActivity extends AppCompatActivity {

    private static final String TAG = "ChiTietMucTieu";

    private TextView txt_m9_ten;
    private TextView txt_m10_datichluy;
    private TextView txt_m10_conthieu;
    private TextView txt_m10_songaycon;
    private TextView txt_m9_hanchot;
    private TextView txt_m10_phantram;
    private ProgressBar progress_m10_tiendo;
    private Button btn_m10_boong;

    private FirebaseFirestore db;
    private DocumentReference mucTieuDocRef;
    private ListenerRegistration mucTieuListener;

    private String mucTieuId;
    private MucTieuTietKiem mucTieuHienTai;

    private final NumberFormat formatterTien =
            NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chitietmuctieu);

        anhXa();

        mucTieuId = getIntent().getStringExtra("id");

        if (mucTieuId == null || mucTieuId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy mục tiêu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        String uid = (user != null) ? user.getUid() : "guest";
        mucTieuDocRef = db.collection("users").document(uid).collection(FirestoreConst.COLLECTION_MUC_TIEU)
                .document(mucTieuId);

        // Nút back trên Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_m10_chitiet);
        toolbar.setNavigationOnClickListener(v -> finish());

        btn_m10_boong.setOnClickListener(v -> hienThiHopThoaiBoOng());
    }

    private void anhXa() {
        txt_m9_ten = findViewById(R.id.txt_m10_ten);
        txt_m10_datichluy = findViewById(R.id.txt_m10_datichluy);
        txt_m10_conthieu = findViewById(R.id.txt_m10_conthieu);
        txt_m10_songaycon = findViewById(R.id.txt_m10_songaycon);
        txt_m9_hanchot = findViewById(R.id.txt_m10_hanchot);
        txt_m10_phantram = findViewById(R.id.txt_m10_phantram);
        progress_m10_tiendo = findViewById(R.id.progress_m10_tiendo);
        btn_m10_boong = findViewById(R.id.btn_m10_boong);
    }

    @Override
    protected void onStart() {
        super.onStart();
        dangKyLangNgheChiTiet();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mucTieuListener != null) {
            mucTieuListener.remove();
            mucTieuListener = null;
        }
    }

    /**
     * Lắng nghe real-time đúng 1 document mục tiêu. Nhờ vậy nếu số tiền
     * hiện tại thay đổi (do transaction bỏ ống thành công) thì UI sẽ tự
     * cập nhật lại ngay mà không cần gọi lại thủ công.
     */
    private void dangKyLangNgheChiTiet() {
        mucTieuListener = mucTieuDocRef.addSnapshotListener(
                (DocumentSnapshot snapshot, FirebaseFirestoreException error) -> {

                    if (error != null) {
                        Log.e(TAG, "Lỗi lắng nghe chi tiết mục tiêu", error);
                        return;
                    }

                    if (snapshot == null || !snapshot.exists()) {
                        Toast.makeText(
                                this,
                                "Mục tiêu này đã bị xóa",
                                Toast.LENGTH_SHORT
                        ).show();
                        finish();
                        return;
                    }

                    mucTieuHienTai = snapshot.toObject(MucTieuTietKiem.class);

                    if (mucTieuHienTai != null) {
                        mucTieuHienTai.setId(snapshot.getId());
                        capNhatGiaoDien(mucTieuHienTai);
                    }
                }
        );
    }

    private void capNhatGiaoDien(MucTieuTietKiem mucTieu) {
        txt_m9_ten.setText(mucTieu.getTenMucTieu());

        long conThieu = mucTieu.getSoTienMucTieu() - mucTieu.getSoTienHienTai();
        if (conThieu < 0) {
            conThieu = 0;
        }

        txt_m10_datichluy.setText(
                "Đã tích lũy: +" + formatterTien.format(mucTieu.getSoTienHienTai()) + " đ"
        );

        txt_m10_conthieu.setText(
                "Còn thiếu: -" + formatterTien.format(conThieu) + " đ"
        );

        txt_m9_hanchot.setText("Hạn chót: " + mucTieu.getNgayHanChot());

        int phanTram = mucTieu.tinhPhanTram();
        progress_m10_tiendo.setProgress(phanTram);
        txt_m10_phantram.setText(phanTram + "%");

        txt_m10_songaycon.setText(tinhChuoiSoNgayConLai(mucTieu.getNgayHanChot()));
    }

    private String tinhChuoiSoNgayConLai(String ngayHanChot) {
        if (ngayHanChot == null || ngayHanChot.isEmpty()) {
            return "";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            sdf.setLenient(false);

            Date ngayHan = sdf.parse(ngayHanChot);
            if (ngayHan == null) {
                return "";
            }

            long soMs = ngayHan.getTime() - System.currentTimeMillis();
            long soNgay = TimeUnit.MILLISECONDS.toDays(soMs) + 1;

            if (soNgay < 0) {
                return "Đã quá hạn " + Math.abs(soNgay) + " ngày";
            } else if (soNgay == 0) {
                return "Hạn chót là hôm nay";
            } else {
                return "Còn " + soNgay + " ngày";
            }
        } catch (ParseException e) {
            return "";
        }
    }

    /**
     * Hiện Dialog cho người dùng tự gõ số tiền mặt vừa cất đi để ghi nhận
     * vào quỹ tiết kiệm (tương ứng đúng luồng "Bỏ ống thủ công" trong 3.10).
     */
    private void hienThiHopThoaiBoOng() {
        EditText edtSoTien = new EditText(this);
        edtSoTien.setInputType(InputType.TYPE_CLASS_NUMBER);
        edtSoTien.setHint("Ví dụ: 500.000");

        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        edtSoTien.setPadding(padding, padding, padding, padding);
        edtSoTien.setGravity(Gravity.START);

        dinhDangTien(edtSoTien);

        new AlertDialog.Builder(this)
                .setTitle("Bỏ ống tiết kiệm")
                .setMessage("Nhập số tiền mặt bạn vừa cất riêng đi:")
                .setView(edtSoTien)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String text = edtSoTien.getText().toString()
                            .replace(".", "")
                            .trim();

                    if (text.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long soTien;
                    try {
                        soTien = Long.parseLong(text);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (soTien <= 0) {
                        Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boOngTietKiem(soTien);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Tương tự dinhDangTien() bên TaoMucTieuActivity: tự thêm dấu chấm ngăn cách hàng nghìn
    private void dinhDangTien(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {

            private boolean isEditing;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing) return;
                isEditing = true;

                String text = s.toString().replace(".", "");

                if (!text.isEmpty()) {
                    try {
                        long value = Long.parseLong(text);

                        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                        symbols.setGroupingSeparator('.');

                        DecimalFormat formatter = new DecimalFormat("#,###", symbols);

                        editText.setText(formatter.format(value));
                        editText.setSelection(editText.getText().length());
                    } catch (NumberFormatException ignored) {
                        // Người dùng gõ số quá lớn / ký tự lạ -> bỏ qua, giữ nguyên text cũ
                    }
                }

                isEditing = false;
            }
        });
    }

    /**
     * Chạy 1 Firestore Transaction để vừa cộng tiền vào mục tiêu, vừa tạo
     * giao dịch chi tiêu tương ứng - đảm bảo 2 thao tác này luôn thành công
     * hoặc thất bại cùng nhau (atomic), tránh trường hợp cộng tiền vào mục
     * tiêu rồi nhưng lại quên trừ số dư khả dụng ở Dashboard.
     */
    private void boOngTietKiem(long soTienBoOng) {
        btn_m10_boong.setEnabled(false);

        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        String uid = (user != null) ? user.getUid() : "guest";

        DocumentReference giaoDichDocRef =
                db.collection("users").document(uid).collection(FirestoreConst.COLLECTION_GIAO_DICH).document();

        db.runTransaction((Transaction.Function<Long>) transaction -> {

            DocumentSnapshot snapshot = transaction.get(mucTieuDocRef);

            Long soTienMucTieu = snapshot.getLong("soTienMucTieu");
            Long soTienHienTaiCu = snapshot.getLong("soTienHienTai");
            String tenMucTieu = snapshot.getString("tenMucTieu");

            long moiSoTienHienTai =
                    (soTienHienTaiCu != null ? soTienHienTaiCu : 0) + soTienBoOng;

            transaction.update(mucTieuDocRef, "soTienHienTai", moiSoTienHienTai);

            GiaoDich giaoDich = new GiaoDich(
                    "Gửi quỹ tiết kiệm: " + tenMucTieu,
                    soTienBoOng,
                    GiaoDich.LOAI_CHI,
                    mucTieuId
            );
            transaction.set(giaoDichDocRef, giaoDich);

            long mucTieuCanDat = soTienMucTieu != null ? soTienMucTieu : 0;
            // Trả về số tiền hiện tại mới để bên ngoài transaction biết có
            // hoàn thành mục tiêu hay chưa.
            return moiSoTienHienTai >= mucTieuCanDat ? moiSoTienHienTai : -1L;

        }).addOnSuccessListener(ketQua -> {
            btn_m10_boong.setEnabled(true);

            Toast.makeText(
                    this,
                    "Đã bỏ ống " + formatterTien.format(soTienBoOng) + " đ",
                    Toast.LENGTH_SHORT
            ).show();

            if (ketQua != null && ketQua >= 0) {
                hienThiChucMungHoanThanh();
            }

        }).addOnFailureListener(e -> {
            btn_m10_boong.setEnabled(true);
            Log.e(TAG, "Bỏ ống tiết kiệm thất bại", e);
            Toast.makeText(
                    this,
                    "Bỏ ống thất bại: " + e.getMessage(),
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void hienThiChucMungHoanThanh() {
        new AlertDialog.Builder(this)
                .setTitle("Chúc mừng!")
                .setMessage("Bạn đã hoàn thành mục tiêu tiết kiệm này 🎉")
                .setCancelable(false)
                .setPositiveButton("Về trang chủ", (dialog, which) -> {
                    // TODO: Khi có Dashboard (M3) thật, đổi MainActivity.class
                    // thành DashboardActivity.class ở đây.
                    Intent intent = new Intent(
                            ChiTietMucTieuActivity.this,
                            MainActivity.class
                    );
                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );
                    startActivity(intent);
                    finish();
                })
                .show();
    }
}
