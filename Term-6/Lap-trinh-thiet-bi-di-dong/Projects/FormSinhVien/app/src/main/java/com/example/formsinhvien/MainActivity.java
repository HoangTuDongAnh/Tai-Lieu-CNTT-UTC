package com.example.formsinhvien;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView listViewDs;
    ArrayList<SinhVien> data;
    SinhVienAdapter adapter;

    static final int REQ_EDIT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        listViewDs = (ListView) findViewById(R.id.listViewDs);

        data = new ArrayList<>();
        data.add(new SinhVien(101, "Nguyễn Văn A"));
        data.add(new SinhVien(102, "Trần Thị B"));

        adapter = new SinhVienAdapter(this, data);
        listViewDs.setAdapter(adapter);

        listViewDs.setOnItemClickListener((parent, view, position, id) -> {

            SinhVien sv = data.get(position);

            Intent intent = new Intent(MainActivity.this, MainActivity2.class);

            // Truyền dữ liệu
            intent.putExtra("SBD", sv.soBD);
            intent.putExtra("HOTEN", sv.hoTen);

            // Lưu chỉ số item
            intent.putExtra("POSITION", position);

            startActivityForResult(intent, REQ_EDIT);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);

        if (requestCode == REQ_EDIT && resultCode == RESULT_OK) {

            int position = dataIntent.getIntExtra("POSITION", -1);
            int soBD = dataIntent.getIntExtra("SBD", -1);
            String hoTen = dataIntent.getStringExtra("HOTEN");

            if (position != -1) {
                data.set(position, new SinhVien(soBD, hoTen));
                adapter.notifyDataSetChanged();
            }
        }
    }

}