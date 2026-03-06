package com.example.sqliteexample;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    EditText editTextId, editTextName, editTextYearOB;
    TextView textViewData;
    Button btnInsert, btnUpdate, btnDelete, btnDisplay;
    MyDBHelper dbHelper = new MyDBHelper(this);

    @Override
    protected void onStart() {
        super.onStart();
        dbHelper.openDB();
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbHelper.closeDB();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        InitWidget();

        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long resultInsert = dbHelper.Insert(editTextName.getText().toString(), Integer.parseInt(editTextYearOB.getText().toString()));
                if (resultInsert == -1) {
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this, "Inserted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuffer buffer = new StringBuffer();
                Cursor cursor = dbHelper.DisplayAll();

                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    buffer.append(cursor.getString(cursor.getColumnIndexOrThrow(MyDBHelper.getID())));
                    buffer.append(" - ");
                    buffer.append(cursor.getString(cursor.getColumnIndexOrThrow(MyDBHelper.getNAME())));
                    buffer.append(" - ");
                    buffer.append(cursor.getString(cursor.getColumnIndexOrThrow(MyDBHelper.getYEAROB())));
                    buffer.append("\n");
                }

                textViewData.setText(buffer);
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long resultUpdate = dbHelper.Update(
                        Integer.parseInt(editTextId.getText().toString()),
                    editTextName.getText().toString(),
                    Integer.parseInt((editTextYearOB.getText().toString()))
                );
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long resultDelete = dbHelper.Delete(
                        Integer.parseInt(editTextId.getText().toString())
                );

                if (resultDelete == 0) {
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void InitWidget() {
        editTextId = (EditText) findViewById(R.id.editTextId);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextYearOB = (EditText) findViewById(R.id.editTextYearOB);
        textViewData = (TextView) findViewById(R.id.textViewData);
        btnInsert = (Button) findViewById(R.id.buttonInsert);
        btnUpdate = (Button) findViewById(R.id.buttonUpdate);
        btnDelete = (Button) findViewById(R.id.buttonDelete);
        btnDisplay = (Button) findViewById(R.id.buttonDisplay);
    }
}