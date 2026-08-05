package com.example.fintrack;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

public class MucTieuTietKiem {

    // Không lưu vào Firestore, chỉ dùng ở phía app để biết đang thao tác
    // với document nào (Firestore tự sinh ID dạng chuỗi ngẫu nhiên)
    @Exclude
    private String id;

    private String tenMucTieu;
    private long soTienMucTieu;
    private long soTienHienTai;
    private String ngayHanChot;

    // Firestore tự động gán thời gian server khi tạo document mới,
    // dùng để sắp xếp danh sách theo thứ tự tạo mới nhất
    @ServerTimestamp
    private Timestamp ngayTao;

    public MucTieuTietKiem() {
        // Firebase cần constructor rỗng để deserialize document -> object
    }

    public MucTieuTietKiem(String tenMucTieu,
                           long soTienMucTieu,
                           long soTienHienTai,
                           String ngayHanChot) {
        this.tenMucTieu = tenMucTieu;
        this.soTienMucTieu = soTienMucTieu;
        this.soTienHienTai = soTienHienTai;
        this.ngayHanChot = ngayHanChot;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Timestamp ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getTenMucTieu() {
        return tenMucTieu;
    }

    public void setTenMucTieu(String tenMucTieu) {
        this.tenMucTieu = tenMucTieu;
    }

    public long getSoTienMucTieu() {
        return soTienMucTieu;
    }

    public void setSoTienMucTieu(long soTienMucTieu) {
        this.soTienMucTieu = soTienMucTieu;
    }

    public long getSoTienHienTai() {
        return soTienHienTai;
    }

    public void setSoTienHienTai(long soTienHienTai) {
        this.soTienHienTai = soTienHienTai;
    }

    public String getNgayHanChot() {
        return ngayHanChot;
    }

    public void setNgayHanChot(String ngayHanChot) {
        this.ngayHanChot = ngayHanChot;
    }

    public int tinhPhanTram() {
        if (soTienMucTieu <= 0) {
            return 0;
        }

        int phanTram = (int) ((soTienHienTai * 100) / soTienMucTieu);

        return Math.min(phanTram, 100);
    }
}