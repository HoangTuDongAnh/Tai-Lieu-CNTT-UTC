package com.example.vetaudemo;

public class Ticket {
    private int maVe;
    private String gaDi;
    private String gaDen;
    private float donGia;
    private boolean loaiVe; // true = khứ hồi, false = một chiều

    public Ticket() {
    }

    public Ticket(int maVe, String gaDi, String gaDen, float donGia, boolean loaiVe) {
        this.maVe = maVe;
        this.gaDi = gaDi;
        this.gaDen = gaDen;
        this.donGia = donGia;
        this.loaiVe = loaiVe;
    }

    public int getMaVe() {
        return maVe;
    }

    public void setMaVe(int maVe) {
        this.maVe = maVe;
    }

    public String getGaDi() {
        return gaDi;
    }

    public void setGaDi(String gaDi) {
        this.gaDi = gaDi;
    }

    public String getGaDen() {
        return gaDen;
    }

    public void setGaDen(String gaDen) {
        this.gaDen = gaDen;
    }

    public float getDonGia() {
        return donGia;
    }

    public void setDonGia(float donGia) {
        this.donGia = donGia;
    }

    public boolean isLoaiVe() {
        return loaiVe;
    }

    public void setLoaiVe(boolean loaiVe) {
        this.loaiVe = loaiVe;
    }

    // Hàm tự động tính tiền theo loại vé
    public float tinhThanhTien() {
        return loaiVe ? donGia * 2 : donGia;
    }

    public String getTenLoaiVe() {
        return loaiVe ? "Khứ Hồi" : "Một Chiều";
    }
}