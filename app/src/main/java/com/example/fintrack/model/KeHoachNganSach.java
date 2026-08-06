package com.example.fintrack.model;

import com.google.firebase.database.Exclude;
import java.io.Serializable;
import java.util.ArrayList;

public class KeHoachNganSach implements Serializable {
    private String thang;
    private long tongThuNhap;
    private ArrayList<PhongBi> danhSachPhongBi;
    private long thoiGianCapNhat;

    public KeHoachNganSach() {
        this.danhSachPhongBi = new ArrayList<>();
    }

    public KeHoachNganSach(String thang, long tongThuNhap, ArrayList<PhongBi> danhSachPhongBi, long thoiGianCapNhat) {
        this.thang = thang;
        this.tongThuNhap = tongThuNhap;
        this.danhSachPhongBi = danhSachPhongBi != null ? danhSachPhongBi : new ArrayList<>();
        this.thoiGianCapNhat = thoiGianCapNhat;
    }

    public String getThang() { return thang; }
    public void setThang(String thang) { this.thang = thang; }

    public long getTongThuNhap() { return tongThuNhap; }
    public void setTongThuNhap(long tongThuNhap) { this.tongThuNhap = tongThuNhap; }

    public ArrayList<PhongBi> getDanhSachPhongBi() { return danhSachPhongBi; }
    public void setDanhSachPhongBi(ArrayList<PhongBi> danhSachPhongBi) { this.danhSachPhongBi = danhSachPhongBi; }

    public long getThoiGianCapNhat() { return thoiGianCapNhat; }
    public void setThoiGianCapNhat(long thoiGianCapNhat) { this.thoiGianCapNhat = thoiGianCapNhat; }

    @Exclude
    public long tinhTongPhanBo() {
        long tong = 0;
        if (danhSachPhongBi != null) {
            for (PhongBi phongBi : danhSachPhongBi) {
                tong += phongBi.getHanMuc();
            }
        }
        return tong;
    }

    @Exclude
    public long tinhSoDuConLai() {
        return tongThuNhap - tinhTongPhanBo();
    }
}
