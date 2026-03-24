package com.example.nhahangdemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    SearchView searchView;
    ListView lvNhaHang;
    Button btnAdd;

    ArrayList<NhaHang> dsNhaHang;
    ArrayList<NhaHang> dsGoc;

    NhaHangAdapter adapter;
    DatabaseHelper databaseHelper;

    NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchView = findViewById(R.id.searchView);
        lvNhaHang = findViewById(R.id.lvNhaHang);
        btnAdd = findViewById(R.id.btnAdd);

        databaseHelper = new DatabaseHelper(this);

        dsNhaHang = new ArrayList<>();
        dsGoc = new ArrayList<>();

        adapter = new NhaHangAdapter(this, dsNhaHang);
        lvNhaHang.setAdapter(adapter);

        loadData();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseHelper.deleteAllNhaHang();
                databaseHelper.insertSampleData();
                loadData();

                Toast.makeText(MainActivity.this,
                        getString(R.string.toast_add_success),
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Tìm kiếm theo tên
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterByName(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterByName(newText);
                return true;
            }
        });

        // Nhấn giữ để xoá các nhà hàng có điểm thấp hơn
        lvNhaHang.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                NhaHang nhaHangDuocChon = dsNhaHang.get(position);
                double mucDanhGia = nhaHangDuocChon.getDanhGia();

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Xác nhận xoá");
                builder.setMessage("Bạn có muốn xoá tất cả nhà hàng có điểm đánh giá dưới "
                        + mucDanhGia + " không?");
                builder.setPositiveButton("Có", (dialog, which) -> {
                    databaseHelper.deleteNhaHangBelowRating(mucDanhGia);
                    loadData();
                    Toast.makeText(MainActivity.this,
                            "Đã xoá các nhà hàng có điểm dưới " + mucDanhGia,
                            Toast.LENGTH_SHORT).show();
                });

                builder.setNegativeButton("Không", (dialog, which) -> dialog.dismiss());
                builder.show();

                return true;
            }
        });

        // Broadcast Receiver theo dõi mạng
        networkChangeReceiver = new NetworkChangeReceiver();
    }

    private void loadData() {
        dsGoc.clear();
        dsGoc.addAll(databaseHelper.getAllNhaHang());

        dsNhaHang.clear();
        dsNhaHang.addAll(dsGoc);

        adapter.notifyDataSetChanged();
    }

    private void filterByName(String keyword) {
        dsNhaHang.clear();

        if (keyword == null || keyword.trim().isEmpty()) {
            dsNhaHang.addAll(dsGoc);
        } else {
            String tuKhoa = keyword.toLowerCase().trim();

            for (NhaHang nhaHang : dsGoc) {
                if (nhaHang.getTenNhaHang().toLowerCase().contains(tuKhoa)) {
                    dsNhaHang.add(nhaHang);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(networkChangeReceiver);
    }
}