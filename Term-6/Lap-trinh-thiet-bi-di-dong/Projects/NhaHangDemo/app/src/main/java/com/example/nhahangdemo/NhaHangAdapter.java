package com.example.nhahangdemo;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class NhaHangAdapter extends ArrayAdapter<NhaHang> {

    Activity context;
    ArrayList<NhaHang> list;

    public NhaHangAdapter(Activity context, ArrayList<NhaHang> list) {
        super(context, R.layout.item_nhahang, list);
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View row = inflater.inflate(R.layout.item_nhahang, null, true);

        TextView txtTen = row.findViewById(R.id.txtTen);
        TextView txtDiaChi = row.findViewById(R.id.txtDiaChi);
        TextView txtDanhGia = row.findViewById(R.id.txtDanhGia);

        NhaHang nhaHang = list.get(position);

        txtTen.setText(nhaHang.getTenNhaHang());
        txtDiaChi.setText(nhaHang.getDiaChi());
        txtDanhGia.setText(String.valueOf(nhaHang.getDanhGia()));

        return row;
    }
}