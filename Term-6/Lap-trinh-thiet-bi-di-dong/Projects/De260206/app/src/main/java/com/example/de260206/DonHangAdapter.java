package com.example.de260206;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DonHangAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<DonHang> list;
    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");
    private final SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public DonHangAdapter(Context context, ArrayList<DonHang> list) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_donhang, parent, false);
        }

        TextView txtTenHang = convertView.findViewById(R.id.txtTenHang);
        TextView txtLoaiGiao = convertView.findViewById(R.id.txtLoaiGiao);
        TextView txtNgayDat = convertView.findViewById(R.id.txtNgayDat);
        TextView txtGiaHang = convertView.findViewById(R.id.txtGiaHang);
        TextView txtPhiVanChuyen = convertView.findViewById(R.id.txtPhiVanChuyen);
        TextView txtThanhTien = convertView.findViewById(R.id.txtThanhTien);

        DonHang donHang = list.get(position);
        txtTenHang.setText(donHang.getTenHang());
        txtLoaiGiao.setText(donHang.isGiaoNhanh() ? "Nhanh" : "Thường");
        txtNgayDat.setText(formatDisplayDate(donHang.getNgayDat()));
        txtGiaHang.setText("Giá: " + formatMoney(donHang.getGiaHang()));
        txtPhiVanChuyen.setText("Phí VC: " + formatMoney(donHang.tinhPhiVanChuyen()));
        txtThanhTien.setText("Thành tiền: " + formatMoney(donHang.tinhThanhTien()));

        if (position % 2 == 0) {
            convertView.setBackgroundColor(Color.rgb(238, 238, 238));
        } else {
            convertView.setBackgroundColor(Color.rgb(58, 58, 58));
        }

        int textColor = position % 2 == 0 ? Color.BLACK : Color.WHITE;
        txtTenHang.setTextColor(textColor);
        txtLoaiGiao.setTextColor(textColor);
        txtNgayDat.setTextColor(textColor);
        txtGiaHang.setTextColor(textColor);
        txtPhiVanChuyen.setTextColor(textColor);
        txtThanhTien.setTextColor(textColor);

        return convertView;
    }

    private String formatDisplayDate(String dbDate) {
        try {
            Date date = dbDateFormat.parse(dbDate);
            if (date != null) {
                return displayDateFormat.format(date);
            }
        } catch (ParseException ignored) {
        }
        return dbDate;
    }

    private String formatMoney(float value) {
        return moneyFormat.format(value).replace(',', ' ');
    }
}
