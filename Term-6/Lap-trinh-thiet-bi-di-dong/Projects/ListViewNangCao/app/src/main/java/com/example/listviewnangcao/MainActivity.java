package com.example.listviewnangcao;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listViewSinhVien;
    ArrayList<SinhVien> arrayListSinhVien;

    EditText editTextHoTen, editTextNamSinh;
    Button btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        listViewSinhVien = (ListView) findViewById(R.id.listViewSinhVien);

        // tao du lieu
        arrayListSinhVien = new ArrayList<SinhVien>();
        arrayListSinhVien.add(new SinhVien("Nguyen Van A", 1991, R.drawable.anh1));
        arrayListSinhVien.add(new SinhVien("Tran Van B", 1997, R.drawable.anh2));
        arrayListSinhVien.add(new SinhVien("Nguyen Thi C", 1999, R.drawable.anh3));

        // tao adapter
        SinhVienAdapter adapter = new SinhVienAdapter(MainActivity.this, R.layout.listitem, arrayListSinhVien);

        listViewSinhVien.setAdapter(adapter);

        listViewSinhVien.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, arrayListSinhVien.get(position).hoTen + " "
                        + arrayListSinhVien.get(position).namSinh.toString(), Toast.LENGTH_LONG).show();
            }
        });

        editTextHoTen = (EditText) findViewById(R.id.editTextHoTen);
        editTextNamSinh = (EditText) findViewById(R.id.editTextNamSinh);
        btnAdd = (Button) findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ten = editTextHoTen.getText().toString().trim();
                String namSinhStr = editTextNamSinh.getText().toString().trim();

                if (ten.isEmpty() || namSinhStr.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int namSinh = Integer.parseInt(namSinhStr);

                arrayListSinhVien.add(new SinhVien(ten, namSinh, R.drawable.ic_launcher_background));

                adapter.notifyDataSetChanged();

                editTextHoTen.setText("");
                editTextNamSinh.setText("");
            }
        });
    }
}