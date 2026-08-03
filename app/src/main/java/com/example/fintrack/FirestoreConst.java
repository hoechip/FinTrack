package com.example.fintrack;

/**
 * Gom tên các collection Firestore vào 1 chỗ để tránh gõ nhầm chuỗi ("magic string")
 * ở nhiều màn hình khác nhau.
 */
public class FirestoreConst {
    // Collection lưu danh sách mục tiêu tiết kiệm (M9, M10)
    public static final String COLLECTION_MUC_TIEU = "MucTieuTietKiem";

    // Collection lưu lịch sử giao dịch thu/chi chung của toàn app (M3, M10, M11...)
    public static final String COLLECTION_GIAO_DICH = "GiaoDich";
}
