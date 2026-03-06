package com.example.intent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Button btnGo;

    EditText editTextA, editTextB;

    TextView textViewKq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        btnGo = (Button) findViewById(R.id.btnGo);
        editTextA = (EditText) findViewById(R.id.editTextA);
        editTextB = (EditText) findViewById(R.id.editTextB);
        textViewKq = (TextView) findViewById(R.id.textViewKq);

//        Intent intent1 = getIntent();
//        int kq = getIntent().getIntExtra("ketqua", 0);
//        textViewKq.setText(String.valueOf(kq));


        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
//
//                String s1 = editTextA.getText().toString();
//                String s2 = editTextB.getText().toString();
//
//                // Cach 1: Gui truc tiep vao intent
//                // intent.putExtra("dataA", s);
//
//                // Cach 2: Su dung bundle
//                Bundle bundle = new Bundle();
//                bundle.putString("dataA", s1);
//                bundle.putString("dataB", s2);
//
//                intent.putExtra("package", bundle);
//
//                startActivity(intent);

                String ten = editTextA.getText().toString().trim();

                if (ten.isEmpty()) return;

                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                intent.putExtra("ten", ten);

                startActivityForResult(intent, 1);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            String ketQua = data.getStringExtra("ketqua");
            textViewKq.setText(ketQua);
        }
    }

}