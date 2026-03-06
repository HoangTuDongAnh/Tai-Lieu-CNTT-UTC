package com.example.doitien;

import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TextView textViewKhachHang, textViewSoTienVND, textViewQuyDoi, textViewKetQua, textViewTongSoKH, textViewTongSoTien;
    EditText editTextKhachHang, editTextSoTienVND, editTextNgay;
    RadioButton rdbUSD, rdbEUR, rdbNDT;
    Button btnSubmit;

    ListView listViewDanhSach;

    ArrayList<String> arrayListDs;
    ArrayAdapter<String> adapterDs;

    int tongSoNguoi = 0;
    BigDecimal tongSoTien = new BigDecimal("0");

    LocalDate today = LocalDate.now();
    String ngayHienTai = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);




        InitWidget();
        arrayListDs = new ArrayList<String>();

        adapterDs = new ArrayAdapter<>(
                MainActivity.this,
                android.R.layout.simple_list_item_1,
                arrayListDs
        );

        listViewDanhSach.setAdapter(adapterDs);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hoTen = editTextKhachHang.getText().toString().trim();
                String ngay = editTextNgay.getText().toString().trim();
                String vndStr = editTextSoTienVND.getText().toString().trim();

                if (vndStr.isEmpty()) {
                    editTextSoTienVND.setError("Vui lòng nhập số tiền");
                    return;
                }

                if (!rdbUSD.isChecked() && !rdbEUR.isChecked() && !rdbNDT.isChecked()) {
                    textViewKetQua.setText("Vui lòng chọn loại tiền cần đổi");
                    return;
                }

                BigDecimal vnd;
                try {
                    vnd = new BigDecimal(vndStr);
                } catch (NumberFormatException e) {
                    editTextSoTienVND.setError("Số tiền không hợp lệ");
                    return;
                }

                StringBuilder result = new StringBuilder("VND sang ");
                StringBuilder ds = new StringBuilder();

                if (rdbUSD.isChecked()) {
                    result.append("USD: ").append(CurrencyConverter.convertVndToUsd(vnd));
                } else if (rdbEUR.isChecked()) {
                    result.append("EUR: ").append(CurrencyConverter.convertVndToEur(vnd));
                } else {
                    result.append("NDT: ").append(CurrencyConverter.convertVndToCny(vnd));
                }

                textViewKetQua.setText(result.toString());

                ds.append(hoTen)
                        .append(" | ")
                        .append(vnd).append(" VND | ")
                        .append(result)
                        .append(" | ")
                        .append(ngay);

                arrayListDs.add(ds.toString());
                adapterDs.notifyDataSetChanged();

                if (ngay.equals(ngayHienTai)) {
                    tongSoTien = tongSoTien.add(vnd);
                    tongSoNguoi = arrayListDs.size();
                }

                textViewTongSoKH.setText(String.valueOf(tongSoNguoi));
                textViewTongSoTien.setText(tongSoTien.toString());
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void InitWidget() {
        textViewKetQua = (TextView) findViewById(R.id.textViewKetQua);
        textViewTongSoKH = (TextView) findViewById(R.id.textViewTongSoKH);
        textViewTongSoTien = (TextView) findViewById(R.id.textViewTongSoTienVND);
        editTextKhachHang = (EditText) findViewById(R.id.editTextKhachHang);
        editTextSoTienVND = (EditText) findViewById(R.id.editTextSoTienVND);
        editTextNgay = (EditText) findViewById(R.id.editTextNgay);
        rdbUSD = (RadioButton) findViewById(R.id.rdbUSD);
        rdbEUR = (RadioButton) findViewById(R.id.rdbEUR);
        rdbNDT = (RadioButton) findViewById(R.id.rdbNDT);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        listViewDanhSach = (ListView) findViewById(R.id.listView);
    }
}

class CurrencyRates {
    // Tỷ giá: 1 đơn vị ngoại tệ = X VND
    public static final BigDecimal VND_PER_USD = new BigDecimal("26077");
    public static final BigDecimal VND_PER_EUR = new BigDecimal("30348");
    public static final BigDecimal VND_PER_CNY = new BigDecimal("3700");
}

class CurrencyConverter {

    public static BigDecimal convertVndToUsd(BigDecimal vnd) {
        return vnd.divide(CurrencyRates.VND_PER_USD, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal convertVndToEur(BigDecimal vnd) {
        return vnd.divide(CurrencyRates.VND_PER_EUR, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal convertVndToCny(BigDecimal vnd) {
        return vnd.divide(CurrencyRates.VND_PER_CNY, 2, RoundingMode.HALF_UP);
    }
}