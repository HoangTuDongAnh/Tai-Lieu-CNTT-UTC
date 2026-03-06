package com.example.onthi1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SinhVienAdapter extends BaseAdapter {

    Context context;
    ArrayList<SinhVien> list;

    public SinhVienAdapter(Context context, ArrayList<SinhVien> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.item_sinhvien, parent, false);
        }

        TextView txtTen = view.findViewById(R.id.txtTen);
        TextView txtTong = view.findViewById(R.id.txtTongDiem);

        SinhVien sv = list.get(position);

        txtTen.setText(sv.getTen());
        txtTong.setText(String.valueOf(sv.tongDiem()));

        return view;
    }
}