package com.example.fintrack;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * Model đại diện cho 1 bản ghi trong collection "GiaoDich" (lịch sử giao dịch chung).
 * Theo mục 3.10, khi người dùng "Bỏ ống tiết kiệm" thì ngoài việc cộng tiền vào
 * mục tiêu, hệ thống còn phải tự tạo 1 giao dịch chi tiêu ở đây để Dashboard (M3)
 * lấy dữ liệu và trừ đi số dư khả dụng.
 */
public class GiaoDich {

    public static final String LOAI_CHI = "chi";
    public static final String LOAI_THU = "thu";

    private String noiDung;
    private long soTien;
    private String loaiGiaoDich;   // "chi" hoặc "thu"
    private String mucTieuId;      // ID của mục tiêu tiết kiệm liên quan (nếu có)

    @ServerTimestamp
    private Timestamp ngayGiaoDich;

    public GiaoDich() {
        // Firebase cần constructor rỗng
    }

    public GiaoDich(String noiDung, long soTien, String loaiGiaoDich, String mucTieuId) {
        this.noiDung = noiDung;
        this.soTien = soTien;
        this.loaiGiaoDich = loaiGiaoDich;
        this.mucTieuId = mucTieuId;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public long getSoTien() {
        return soTien;
    }

    public void setSoTien(long soTien) {
        this.soTien = soTien;
    }

    public String getLoaiGiaoDich() {
        return loaiGiaoDich;
    }

    public void setLoaiGiaoDich(String loaiGiaoDich) {
        this.loaiGiaoDich = loaiGiaoDich;
    }

    public String getMucTieuId() {
        return mucTieuId;
    }

    public void setMucTieuId(String mucTieuId) {
        this.mucTieuId = mucTieuId;
    }

    public Timestamp getNgayGiaoDich() {
        return ngayGiaoDich;
    }

    public void setNgayGiaoDich(Timestamp ngayGiaoDich) {
        this.ngayGiaoDich = ngayGiaoDich;
    }
}
