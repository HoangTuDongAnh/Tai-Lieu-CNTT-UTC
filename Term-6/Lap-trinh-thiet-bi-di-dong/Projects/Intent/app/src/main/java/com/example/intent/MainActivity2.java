package com.example.intent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity2 extends AppCompatActivity {

    Button btnBack;
    TextView textView1, textView2;

    int kq;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        ArrayList<Nguoi> ds = new ArrayList<>();

        Nguoi n1 = new Nguoi("Hoang");
        n1.addDiem(8);
        n1.addDiem(9);
        n1.addDiem(10);

        Nguoi n2 = new Nguoi("An");
        n2.addDiem(7);
        n2.addDiem(6);

        ds.add(n1);
        ds.add(n2);

        String ten = getIntent().getStringExtra("ten");

        Nguoi ketQuaNguoi = null;
        for (Nguoi n : ds) {
            if (n.getTen().equalsIgnoreCase(ten)) {
                ketQuaNguoi = n;
                break;
            }
        }

        Intent resultIntent = new Intent();

        if (ketQuaNguoi != null) {
            resultIntent.putExtra("ketqua", "" + ketQuaNguoi.getDiem());
        } else {
            resultIntent.putExtra(
                    "ketqua",
                    "Không tìm thấy người tên: " + ten
            );
        }

        setResult(RESULT_OK, resultIntent);
        finish();
    }

//        btnBack = (Button) findViewById(R.id.btnBack);
//        textView1 = (TextView) findViewById(R.id.textView1);
//        textView2 = (TextView) findViewById(R.id.textView2);
//
//        // Cach 1: Gui truc tiep vao intent
//        // Intent intent = getIntent();
//        // String s = intent.getStringExtra("dataA");
//        // textViewKq.setText(s);
//
//        // Cach 2: Su dung bundle
////        Intent intent = getIntent();
////        Bundle bundle = intent.getBundleExtra("package");
////
////        if (bundle != null) {
////            String s1 = bundle.getString("dataA");
////            String s2 = bundle.getString("dataB");
////            kq = Integer.parseInt(s1) + Integer.parseInt(s2);
////            textView1.setText(s1);
////            textView2.setText(s2);
////        }
//
//        btnBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent1 = new Intent(MainActivity2.this, MainActivity.class);
//
//                intent1.putExtra("ketqua", kq);
//
//                startActivity(intent1);
//            }
//        });
}
