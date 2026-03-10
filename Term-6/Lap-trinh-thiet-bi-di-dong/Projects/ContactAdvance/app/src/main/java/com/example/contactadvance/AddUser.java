package com.example.contactadvance;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class AddUser extends AppCompatActivity {
    EditText etId, etName, etPhone;
    Button btnOk, btnCancel;
    private boolean isEdited=false;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        etId = findViewById(R.id.etID);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        btnOk = findViewById(R.id.btnOK);
        btnCancel = findViewById(R.id.btnCancel);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle!=null) {
            isEdited=true;
            int id = bundle.getInt("Id");
            String name = bundle.getString("Name");
            String phone = bundle.getString("Phone");
            etId.setText(String.valueOf(id));
            etName.setText(name);
            etPhone.setText(phone);
            btnOk.setText("Edit");
        }

        btnOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //Tạo intent để trở về MainActivity
                Intent intent = new Intent();
                //Tạo bundle là đối tượng để chứa dữ liệu
                Bundle bundle = new Bundle();
                //bundle hoạt động như một Java Map các phần tử phân biệt theo key
                //bundle có các hàm put... trong đó ... là kiểu dữ liệu tương ứng
                bundle.putInt("Id", Integer.parseInt(etId.getText().toString()));
                bundle.putString("Name", etName.getText().toString());
                bundle.putString("Phone", etPhone.getText().toString());
                intent.putExtras(bundle);
                setResult(isEdited?201:200, intent);
                finish();
            }
        });
        btnCancel.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view) {
                setResult(400);
                finish();
            }
        });
    }
}


