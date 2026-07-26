package com.example.de260206;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_READ_CONTACTS = 100;

    private SearchView searchView;
    private Button btnSapXep;
    private TextView txtTrungBinh;
    private ListView listView;

    private DBHelper dbHelper;
    private ArrayList<DonHang> originalList;
    private ArrayList<DonHang> displayList;
    private DonHangAdapter adapter;
    private DonHang selectedDonHangForContact;

    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");
    private final SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initWidget();
        initData();
        initSearch();
        initSort();
        initLongClickContactSearch();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left + 8, systemBars.top + 8, systemBars.right + 8, systemBars.bottom + 8);
            return insets;
        });
    }

    private void initWidget() {
        searchView = findViewById(R.id.searchView);
        btnSapXep = findViewById(R.id.btnSapXep);
        txtTrungBinh = findViewById(R.id.txtTrungBinh);
        listView = findViewById(R.id.listView);
    }

    private void initData() {
        dbHelper = new DBHelper(this);
        originalList = dbHelper.getAllDonHang();
        displayList = new ArrayList<>(originalList);
        adapter = new DonHangAdapter(this, displayList);
        listView.setAdapter(adapter);
        updateAverageThanhTien();
    }

    private void initSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterByInputDate(query, true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterByInputDate(newText, false);
                return true;
            }
        });
    }

    private void initSort() {
        btnSapXep.setOnClickListener(v -> {
            Collections.sort(displayList, (d1, d2) ->
                    d1.getTenHang().compareToIgnoreCase(d2.getTenHang()));
            adapter.notifyDataSetChanged();
        });
    }

    private void initLongClickContactSearch() {
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            selectedDonHangForContact = displayList.get(position);
            searchContactByName(selectedDonHangForContact.getTenHang());
            return true;
        });
    }

    private void filterByInputDate(String input, boolean showError) {
        String key = input == null ? "" : input.trim();
        displayList.clear();

        if (key.isEmpty() || key.length() < 10) {
            displayList.addAll(originalList);
        } else {
            try {
                inputDateFormat.setLenient(false);
                Date date = inputDateFormat.parse(key);
                String dbDate = dbDateFormat.format(date);
                displayList.addAll(dbHelper.getDonHangFromDate(dbDate));
            } catch (ParseException e) {
                if (showError) {
                    Toast.makeText(this, "Ngày không hợp lệ. Hãy nhập dd/MM/yyyy", Toast.LENGTH_SHORT).show();
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateAverageThanhTien();
    }

    private void updateAverageThanhTien() {
        if (originalList == null || originalList.isEmpty()) {
            txtTrungBinh.setText("0");
            return;
        }

        float total = 0;
        for (DonHang donHang : originalList) {
            total += donHang.tinhThanhTien();
        }
        txtTrungBinh.setText(formatMoney(total / originalList.size()));
    }

    private void searchContactByName(String name) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS
            );
            return;
        }

        String phone = findPhoneNumberByDisplayName(name);
        if (phone != null) {
            Toast.makeText(this, "Trong danh bạ, Số điện thoại của " + name + " là " + phone,
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Không tìm thấy", Toast.LENGTH_SHORT).show();
        }
    }

    private String findPhoneNumberByDisplayName(String name) {
        String result = null;
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = ?";
        String[] args = new String[]{name};

        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                selection,
                args,
                null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = cursor.getString(1);
            }
            cursor.close();
        }

        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && selectedDonHangForContact != null) {
                searchContactByName(selectedDonHangForContact.getTenHang());
            } else {
                Toast.makeText(this, "Cần quyền đọc danh bạ để tìm số điện thoại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String formatMoney(float value) {
        return moneyFormat.format(value).replace(',', ' ');
    }
}
