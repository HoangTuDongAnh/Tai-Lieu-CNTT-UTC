package com.example.de260206;

public class DonHang {
    private String ma;
    private String tenHang;
    private String ngayDat; // Luu theo dinh dang yyyy-MM-dd de de sap xep/so sanh trong SQLite
    private float giaHang;
    private boolean giaoNhanh;

    public DonHang(String ma, String tenHang, String ngayDat, float giaHang, boolean giaoNhanh) {
        this.ma = ma;
        this.tenHang = tenHang;
        this.ngayDat = ngayDat;
        this.giaHang = giaHang;
        this.giaoNhanh = giaoNhanh;
    }

    public String getMa() {
        return ma;
    }

    public void setMa(String ma) {
        this.ma = ma;
    }

    public String getTenHang() {
        return tenHang;
    }

    public void setTenHang(String tenHang) {
        this.tenHang = tenHang;
    }

    public String getNgayDat() {
        return ngayDat;
    }

    public void setNgayDat(String ngayDat) {
        this.ngayDat = ngayDat;
    }

    public float getGiaHang() {
        return giaHang;
    }

    public void setGiaHang(float giaHang) {
        this.giaHang = giaHang;
    }

    public boolean isGiaoNhanh() {
        return giaoNhanh;
    }

    public void setGiaoNhanh(boolean giaoNhanh) {
        this.giaoNhanh = giaoNhanh;
    }

    public float tinhPhiVanChuyen() {
        float phi;
        if (giaHang < 1_000_000) {
            phi = (35 + 1) * 2000;
        } else {
            phi = (35 + 1) * 3000;
        }

        if (giaoNhanh) {
            phi += 50_000;
        }

        return phi;
    }

    public float tinhThanhTien() {
        return giaHang + tinhPhiVanChuyen();
    }
}
