package com.example.gridview;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {

    GridView gridView;
    TextView textView;

    MaterialToolbar toolbar;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        gridView = (GridView) findViewById(R.id.gridView);
        textView = (TextView) findViewById(R.id.textView);
        toolbar = (MaterialToolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mydialog();
            }
        });


        // Tạo nguồn dữ liệu
        final String[] kytu = new String[] { "A", "B", "C", "D", "E", "F", "G", "H",
                "I", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, kytu);

        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, kytu[position].toString(), Toast.LENGTH_SHORT).show();
            }
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void mydialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.mydialog);

        // ánh xạ
        EditText editTextUserName = (EditText) dialog.findViewById(R.id.editTextUserName);
        EditText editTextPassword = (EditText) dialog.findViewById(R.id.editTextPassword);

        Button btnOk = (Button) dialog.findViewById(R.id.btnOk);
        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUserName.getText().toString();
                String password = editTextPassword.getText().toString();

                if (username.equals("abcd") && password.equals("1234")) {
                    Toast.makeText(MainActivity.this, "Correct", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.mnuGreen) {
            textView.setBackgroundColor(Color.GREEN);
        }
        if (item.getItemId() == R.id.mnuRed) {
            textView.setBackgroundColor(Color.RED);
        }
        if (item.getItemId() == R.id.mnuYellow) {
            textView.setBackgroundColor(Color.YELLOW);
        }
        return super.onOptionsItemSelected(item);
    }
}