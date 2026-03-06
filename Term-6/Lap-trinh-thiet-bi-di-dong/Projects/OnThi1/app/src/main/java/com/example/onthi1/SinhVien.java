package com.example.onthi1;

public class SinhVien {

    private int maSV;
    private String ten;
    private int diemToan, diemHoa, diemLy;

    public SinhVien(int maSV, String ten, int diemToan, int diemHoa, int diemLy) {
        this.maSV = maSV;
        this.ten = ten;
        this.diemToan = diemToan;
        this.diemHoa = diemHoa;
        this.diemLy = diemLy;
    }

    public int getMaSV() {
        return maSV;
    }

    public String getTen() {
        return ten;
    }

    public int getDiemToan() {
        return diemToan;
    }

    public int getDiemHoa() {
        return diemHoa;
    }

    public int getDiemLy() {
        return diemLy;
    }

    public void setMaSV(int maSV) {
        this.maSV = maSV;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public void setDiemToan(int diemToan) {
        this.diemToan = diemToan;
    }

    public void setDiemHoa(int diemHoa) {
        this.diemHoa = diemHoa;
    }

    public void setDiemLy(int diemLy) {
        this.diemLy = diemLy;
    }

    public int tongDiem() {
        return diemToan + diemHoa + diemLy;
    }
}