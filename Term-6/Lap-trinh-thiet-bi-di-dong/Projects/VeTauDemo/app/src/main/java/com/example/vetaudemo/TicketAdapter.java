package com.example.vetaudemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class TicketAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Ticket> list;

    public TicketAdapter(Context context, ArrayList<Ticket> list) {
        this.context = context;
        this.list = list;
    }

    public void setData(ArrayList<Ticket> list) {
        this.list = list;
        notifyDataSetChanged();
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
        return list.get(position).getMaVe();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_ticket, parent, false);
        }

        TextView txtRoute = convertView.findViewById(R.id.txtRoute);
        TextView txtLoaiVe = convertView.findViewById(R.id.txtLoaiVe);
        TextView txtPrice = convertView.findViewById(R.id.txtPrice);

        Ticket ticket = list.get(position);

        txtRoute.setText(ticket.getGaDi() + " -> " + ticket.getGaDen());
        txtLoaiVe.setText(ticket.getTenLoaiVe());
        txtPrice.setText(String.format(Locale.getDefault(), "%.3f", ticket.tinhThanhTien()));

        return convertView;
    }
}