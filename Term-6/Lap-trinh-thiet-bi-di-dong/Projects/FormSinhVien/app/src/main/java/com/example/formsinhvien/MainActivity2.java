package com.example.formsinhvien;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity2 extends AppCompatActivity {

    EditText editTextSBD, editTextHoTen;
    Button btnSua, btnQuayVe;

    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        InitWidget();

        Intent intent = getIntent();

        int soBD = intent.getIntExtra("SBD", -1);
        String hoTen = intent.getStringExtra("HOTEN");
        position = intent.getIntExtra("POSITION", -1);

        // Đổ dữ liệu lên EditText
        editTextSBD.setText(String.valueOf(soBD));
        editTextHoTen.setText(hoTen);

        btnSua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newSBD = Integer.parseInt(editTextSBD.getText().toString());
                String newHoTen = editTextHoTen.getText().toString();

                Intent resultIntent = new Intent();
                resultIntent.putExtra("POSITION", position);
                resultIntent.putExtra("SBD", newSBD);
                resultIntent.putExtra("HOTEN", newHoTen);

                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        btnQuayVe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void InitWidget() {
        editTextSBD = (EditText) findViewById(R.id.editTextSBD);
        editTextHoTen = (EditText) findViewById(R.id.editTextHoTen);
        btnSua = (Button) findViewById(R.id.buttonSua);
        btnQuayVe = (Button) findViewById(R.id.buttonQuayVe);
    }
}