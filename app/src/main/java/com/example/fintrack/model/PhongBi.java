package com.example.fintrack.model;

import java.io.Serializable;

public class PhongBi implements Serializable {
    private String id;
    private String tenHangMuc;
    private String loaiIcon;
    private long hanMuc;
    private long daTieu;

    public PhongBi() {}

    public PhongBi(String id, String tenHangMuc, String loaiIcon, long hanMuc, long daTieu) {
        this.id = id;
        this.tenHangMuc = tenHangMuc;
        this.loaiIcon = loaiIcon;
        this.hanMuc = hanMuc;
        this.daTieu = daTieu;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenHangMuc() { return tenHangMuc; }
    public void setTenHangMuc(String tenHangMuc) { this.tenHangMuc = tenHangMuc; }

    public String getLoaiIcon() { return loaiIcon; }
    public void setLoaiIcon(String loaiIcon) { this.loaiIcon = loaiIcon; }

    public long getHanMuc() { return hanMuc; }
    public void setHanMuc(long hanMuc) { this.hanMuc = hanMuc; }

    public long getDaTieu() { return daTieu; }
    public void setDaTieu(long daTieu) { this.daTieu = daTieu; }

    public int getPhanTramDaTieu() {
        if (hanMuc <= 0) return daTieu > 0 ? 100 : 0;
        return (int) Math.round(((double) daTieu / (double) hanMuc) * 100);
    }

    public long getSoDuConLai() {
        return hanMuc - daTieu;
    }
}
