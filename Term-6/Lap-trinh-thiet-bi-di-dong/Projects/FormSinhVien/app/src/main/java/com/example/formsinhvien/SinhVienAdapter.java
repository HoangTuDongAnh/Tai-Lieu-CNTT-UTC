package com.example.formsinhvien;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class SinhVienAdapter extends BaseAdapter {

    Context context;
    List<SinhVien> list;

    public SinhVienAdapter(Context context, List<SinhVien> list) {
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
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_sinhvien, parent, false);
        }

        TextView txtSoBD = convertView.findViewById(R.id.txtSoBD);
        TextView txtHoTen = convertView.findViewById(R.id.txtHoTen);

        SinhVien sv = list.get(position);
        txtSoBD.setText("SBD: " + sv.soBD);
        txtHoTen.setText(sv.hoTen);

        return convertView;
    }
}
