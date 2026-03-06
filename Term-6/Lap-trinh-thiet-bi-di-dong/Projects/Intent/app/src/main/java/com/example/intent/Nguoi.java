package com.example.intent;

import java.util.ArrayList;

public class Nguoi {
    private String ten;
    private ArrayList<Integer> diem;

    public Nguoi(String ten) {
        this.ten = ten;
        this.diem = new ArrayList<>();
    }

    public void addDiem(int d) {
        diem.add(d);
    }

    public String getTen() {
        return ten;
    }

    public ArrayList<Integer> getDiem() {
        return diem;
    }
}
