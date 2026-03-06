package com.example.listviewnangcao;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class SinhVienAdapter extends ArrayAdapter<SinhVien> {

    // Lấy Constructor chứa textView và chứa List

    public SinhVienAdapter(@NonNull Context context, int resource, @NonNull List<SinhVien> objects) {
        super(context, resource, objects);
    }

    public SinhVienAdapter(@NonNull Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    // Lấy hàm getView từ Override
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.listitem, parent, false);
        }
        SinhVien sv = getItem(position);
        if (sv != null) {
            // Anh xa
            TextView textViewHoTen = (TextView) v.findViewById(R.id.textViewHoTen);
            TextView textViewNamSinh = (TextView) v.findViewById(R.id.textViewNamSinh);
            ImageView imageView = (ImageView) v.findViewById(R.id.imageViewHinhAnh);

            // gan gia tri
            textViewHoTen.setText(sv.hoTen);
            textViewNamSinh.setText(String.valueOf(sv.namSinh));
            imageView.setImageResource(sv.hinhAnh);
        }
        return v;
    }
}
