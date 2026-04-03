package com.example.vetaudemo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class AddEditTicketActivity extends AppCompatActivity {

    private EditText edtGaDi, edtGaDen, edtDonGia;
    private RadioButton rbKhuHoi, rbMotChieu;
    private RadioGroup radioGroupLoaiVe;
    private TextView txtThanhTien;
    private Button btnSave, btnBack;

    private DatabaseHelper dbHelper;
    private int editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ticket);

        edtGaDi = findViewById(R.id.edtGaDi);
        edtGaDen = findViewById(R.id.edtGaDen);
        edtDonGia = findViewById(R.id.edtDonGia);
        rbKhuHoi = findViewById(R.id.rbKhuHoi);
        rbMotChieu = findViewById(R.id.rbMotChieu);
        radioGroupLoaiVe = findViewById(R.id.radioGroupLoaiVe);
        txtThanhTien = findViewById(R.id.txtThanhTien);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        dbHelper = new DatabaseHelper(this);

        if (getIntent() != null && getIntent().hasExtra("id")) {
            editId = getIntent().getIntExtra("id", -1);
            loadTicketForEdit(editId);
            btnSave.setText("Sửa");
        } else {
            rbMotChieu.setChecked(true);
        }

        edtDonGia.addTextChangedListener(textWatcher);
        radioGroupLoaiVe.setOnCheckedChangeListener((group, checkedId) -> updateThanhTien());

        btnSave.setOnClickListener(v -> saveTicket());
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadTicketForEdit(int id) {
        Ticket ticket = dbHelper.getTicketById(id);
        if (ticket != null) {
            edtGaDi.setText(ticket.getGaDi());
            edtGaDen.setText(ticket.getGaDen());
            edtDonGia.setText(String.valueOf(ticket.getDonGia()));
            if (ticket.isLoaiVe()) {
                rbKhuHoi.setChecked(true);
            } else {
                rbMotChieu.setChecked(true);
            }
            updateThanhTien();
        }
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateThanhTien();
        }

        @Override
        public void afterTextChanged(Editable s) { }
    };

    private void updateThanhTien() {
        String giaStr = edtDonGia.getText().toString().trim();
        if (giaStr.isEmpty()) {
            txtThanhTien.setText("Thành tiền: 0");
            return;
        }

        float donGia = Float.parseFloat(giaStr);
        boolean loaiVe = rbKhuHoi.isChecked();
        float thanhTien = loaiVe ? donGia * 2 : donGia;

        txtThanhTien.setText(String.format(Locale.getDefault(), "Thành tiền: %.3f", thanhTien));
    }

    private void saveTicket() {
        String gaDi = edtGaDi.getText().toString().trim();
        String gaDen = edtGaDen.getText().toString().trim();
        String donGiaStr = edtDonGia.getText().toString().trim();

        if (gaDi.isEmpty() || gaDen.isEmpty() || donGiaStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        float donGia = Float.parseFloat(donGiaStr);
        boolean loaiVe = rbKhuHoi.isChecked();

        Ticket ticket = new Ticket(editId, gaDi, gaDen, donGia, loaiVe);

        if (editId == -1) {
            dbHelper.addTicket(ticket);
            Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.updateTicket(ticket);
            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
        }

        setResult(RESULT_OK);
        finish();
    }
}