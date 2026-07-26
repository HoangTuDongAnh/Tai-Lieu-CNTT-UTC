package com.example.contactadvance;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.contactadvance.database.DatabaseHelper;
import com.example.contactadvance.models.Contact;

public class AddContactActivity extends AppCompatActivity {

    private EditText edtName, edtPhone, edtImage;
    private Button btnSave;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtImage = findViewById(R.id.edtImage);
        btnSave = findViewById(R.id.btnSave);

        databaseHelper = new DatabaseHelper(this);

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String image = edtImage.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "Vui lòng nhập tên và số điện thoại", Toast.LENGTH_SHORT).show();
                return;
            }

            Contact contact = new Contact(name, phone, image);
            long result = databaseHelper.addContact(contact);

            if (result > 0) {
                Toast.makeText(this, "Thêm liên hệ thành công", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Thêm thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }
}