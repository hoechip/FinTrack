package com.example.fintrack;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * M9 - Danh sách mục tiêu tiết kiệm.
 * Toàn bộ CRUD được đồng bộ trực tiếp với Firebase Firestore (collection
 * "MucTieuTietKiem") thay vì lưu cục bộ trong ArrayList như bản cũ.
 *
 * Cơ chế hoạt động:
 *  - addSnapshotListener lắng nghe real-time: bất cứ khi nào dữ liệu trên
 *    Firestore thay đổi (thêm/sửa/xóa từ chính máy này hoặc từ thiết bị khác),
 *    RecyclerView sẽ tự cập nhật lại danh sách mà không cần code thủ công.
 *  - TaoMucTieuActivity chỉ có nhiệm vụ thu thập dữ liệu nhập từ Form rồi trả
 *    về qua Intent; việc ghi lên Firestore (add/update) được thực hiện tại đây.
 */
public class M9DanhSachMucTieuActivity extends AppCompatActivity {

    private static final String TAG = "M9DanhSachMucTieu";

    private RecyclerView rv_m9_danhsach;
    private Button btn_m9_add;

    private MucTieuAdapter adapter;
    private List<MucTieuTietKiem> danhSachMucTieu;

    private FirebaseFirestore db;
    private CollectionReference mucTieuRef;
    private ListenerRegistration mucTieuListener;

    private ActivityResultLauncher<Intent> taoMucTieuLauncher;
    private ActivityResultLauncher<Intent> suaMucTieuLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_m9_danhsachmuctieu);

        rv_m9_danhsach = findViewById(R.id.rv_m9_danhsach);
        btn_m9_add = findViewById(R.id.btn_m9_add);

        danhSachMucTieu = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        mucTieuRef = db.collection(FirestoreConst.COLLECTION_MUC_TIEU);

        adapter = new MucTieuAdapter(
                danhSachMucTieu,
                new MucTieuAdapter.OnItemClickListener() {

                    @Override
                    public void onItemClick(MucTieuTietKiem mucTieu) {
                        moManHinhChiTiet(mucTieu);
                    }

                    @Override
                    public void onSuaClick(MucTieuTietKiem mucTieu) {
                        moManHinhSua(mucTieu);
                    }

                    @Override
                    public void onXoaClick(MucTieuTietKiem mucTieu) {
                        hienThiHopThoaiXoa(mucTieu);
                    }
                }
        );

        rv_m9_danhsach.setLayoutManager(new LinearLayoutManager(this));
        rv_m9_danhsach.setAdapter(adapter);

        dangKyActivityResult();

        btn_m9_add.setOnClickListener(view -> {
            Intent intent = new Intent(
                    M9DanhSachMucTieuActivity.this,
                    TaoMucTieuActivity.class
            );
            intent.putExtra("cheDoSua", false);
            taoMucTieuLauncher.launch(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Gắn listener khi màn hình hiển thị (onStart) và gỡ ở onStop để
        // tránh rò rỉ bộ nhớ + tốn quota đọc dữ liệu khi màn hình không hiển thị.
        dangKyLangNgheDuLieu();
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
     * Lắng nghe real-time toàn bộ collection MucTieuTietKiem, sắp xếp theo
     * ngày tạo giảm dần (mục tiêu mới tạo hiển thị lên đầu danh sách).
     */
    private void dangKyLangNgheDuLieu() {
        Query query = mucTieuRef.orderBy("ngayTao", Query.Direction.DESCENDING);

        mucTieuListener = query.addSnapshotListener(
                (QuerySnapshot snapshot, FirebaseFirestoreException error) -> {

                    if (error != null) {
                        Log.e(TAG, "Lỗi lắng nghe dữ liệu Firestore", error);
                        Toast.makeText(
                                M9DanhSachMucTieuActivity.this,
                                "Không tải được dữ liệu: " + error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    if (snapshot == null) {
                        return;
                    }

                    danhSachMucTieu.clear();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        MucTieuTietKiem mucTieu = doc.toObject(MucTieuTietKiem.class);

                        if (mucTieu != null) {
                            // id không được lưu trong document (đã @Exclude) nên
                            // phải gán thủ công từ doc.getId() sau khi parse.
                            mucTieu.setId(doc.getId());
                            danhSachMucTieu.add(mucTieu);
                        }
                    }

                    adapter.notifyDataSetChanged();
                }
        );
    }

    private void dangKyActivityResult() {
        // Nhận dữ liệu sau khi tạo mục tiêu mới -> ghi thêm document lên Firestore
        taoMucTieuLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                        return;
                    }

                    MucTieuTietKiem mucTieuMoi = docDuLieuTuIntent(result.getData());

                    mucTieuRef.add(mucTieuMoi)
                            .addOnSuccessListener(documentReference ->
                                    Toast.makeText(
                                            M9DanhSachMucTieuActivity.this,
                                            "Đã tạo mục tiêu mới",
                                            Toast.LENGTH_SHORT
                                    ).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            M9DanhSachMucTieuActivity.this,
                                            "Tạo mục tiêu thất bại: " + e.getMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show()
                            );
                    // Không cần tự thêm vào danhSachMucTieu/adapter ở đây:
                    // addSnapshotListener ở trên sẽ tự động nhận document mới
                    // và render lại RecyclerView.
                }
        );

        // Nhận dữ liệu sau khi sửa mục tiêu -> update document theo id lên Firestore
        suaMucTieuLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                        return;
                    }

                    Intent data = result.getData();
                    String id = data.getStringExtra("id");

                    if (id == null || id.isEmpty()) {
                        return;
                    }

                    MucTieuTietKiem mucTieuMoi = docDuLieuTuIntent(data);

                    mucTieuRef.document(id)
                            // Chỉ update các trường cần thiết, KHÔNG dùng set() với
                            // object mới toàn bộ để tránh ghi đè mất trường ngayTao
                            // (được Firestore tự sinh lúc tạo document).
                            .update(
                                    "tenMucTieu", mucTieuMoi.getTenMucTieu(),
                                    "soTienMucTieu", mucTieuMoi.getSoTienMucTieu(),
                                    "soTienHienTai", mucTieuMoi.getSoTienHienTai(),
                                    "ngayHanChot", mucTieuMoi.getNgayHanChot()
                            )
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(
                                            M9DanhSachMucTieuActivity.this,
                                            "Đã cập nhật mục tiêu",
                                            Toast.LENGTH_SHORT
                                    ).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            M9DanhSachMucTieuActivity.this,
                                            "Cập nhật thất bại: " + e.getMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show()
                            );
                }
        );
    }

    @NonNull
    private MucTieuTietKiem docDuLieuTuIntent(@NonNull Intent data) {
        String tenMucTieu = data.getStringExtra("tenMucTieu");
        long soTienMucTieu = data.getLongExtra("soTienMucTieu", 0);
        long soTienHienTai = data.getLongExtra("soTienHienTai", 0);
        String ngayHanChot = data.getStringExtra("ngayHanChot");

        return new MucTieuTietKiem(
                tenMucTieu,
                soTienMucTieu,
                soTienHienTai,
                ngayHanChot
        );
    }

    private void moManHinhChiTiet(MucTieuTietKiem mucTieu) {
        Intent intent = new Intent(
                M9DanhSachMucTieuActivity.this,
                ChiTietMucTieuActivity.class
        );
        intent.putExtra("id", mucTieu.getId());
        startActivity(intent);
    }

    private void moManHinhSua(MucTieuTietKiem mucTieu) {
        Intent intent = new Intent(
                M9DanhSachMucTieuActivity.this,
                TaoMucTieuActivity.class
        );

        intent.putExtra("cheDoSua", true);
        intent.putExtra("id", mucTieu.getId());
        intent.putExtra("tenMucTieu", mucTieu.getTenMucTieu());
        intent.putExtra("soTienMucTieu", mucTieu.getSoTienMucTieu());
        intent.putExtra("soTienHienTai", mucTieu.getSoTienHienTai());
        intent.putExtra("ngayHanChot", mucTieu.getNgayHanChot());

        suaMucTieuLauncher.launch(intent);
    }

    private void hienThiHopThoaiXoa(MucTieuTietKiem mucTieu) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa mục tiêu")
                .setMessage(
                        "Bạn có chắc muốn xóa mục tiêu \""
                                + mucTieu.getTenMucTieu()
                                + "\" không?"
                )
                .setPositiveButton("Xóa", (dialog, which) -> xoaMucTieu(mucTieu))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void xoaMucTieu(MucTieuTietKiem mucTieu) {
        if (mucTieu.getId() == null) {
            return;
        }

        mucTieuRef.document(mucTieu.getId())
                .delete()
                .addOnSuccessListener(unused ->
                        Toast.makeText(
                                this,
                                "Đã xóa mục tiêu",
                                Toast.LENGTH_SHORT
                        ).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Xóa thất bại: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }
}
