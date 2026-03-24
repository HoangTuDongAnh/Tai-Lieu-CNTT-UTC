package com.example.nhahangdemo;

public class NhaHang {
    private int maNhaHang;
    private String tenNhaHang;
    private String diaChi;
    private double danhGia;

    public NhaHang() {
    }

    public NhaHang(int maNhaHang, String tenNhaHang, String diaChi, double danhGia) {
        this.maNhaHang = maNhaHang;
        this.tenNhaHang = tenNhaHang;
        this.diaChi = diaChi;
        this.danhGia = danhGia;
    }

    public NhaHang(String tenNhaHang, String diaChi, double danhGia) {
        this.tenNhaHang = tenNhaHang;
        this.diaChi = diaChi;
        this.danhGia = danhGia;
    }

    public int getMaNhaHang() {
        return maNhaHang;
    }

    public void setMaNhaHang(int maNhaHang) {
        this.maNhaHang = maNhaHang;
    }

    public String getTenNhaHang() {
        return tenNhaHang;
    }

    public void setTenNhaHang(String tenNhaHang) {
        this.tenNhaHang = tenNhaHang;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public double getDanhGia() {
        return danhGia;
    }

    public void setDanhGia(double danhGia) {
        this.danhGia = danhGia;
    }
}