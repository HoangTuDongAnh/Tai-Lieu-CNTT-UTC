package com.example.contactadvance;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactadvance.adapters.ContactAdapter;
import com.example.contactadvance.database.DatabaseHelper;
import com.example.contactadvance.models.Contact;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private EditText edtSearch;
    private Button btnAdd, btnDelete;

    private DatabaseHelper databaseHelper;
    private ContactAdapter adapter;
    private List<Contact> contactList;

    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        setSupportActionBar(toolbar);

        databaseHelper = new DatabaseHelper(this);
        contactList = new ArrayList<>(databaseHelper.getAllContacts());

        adapter = new ContactAdapter(this, contactList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        networkChangeReceiver = new NetworkChangeReceiver();

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddContactActivity.class);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> {
            List<Contact> selectedList = adapter.getContactList();
            databaseHelper.deleteSelectedContacts(selectedList);
            loadContacts();
            Toast.makeText(MainActivity.this, "Đã xóa các liên hệ được chọn", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(networkChangeReceiver);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        edtSearch = findViewById(R.id.edtSearch);
        btnAdd = findViewById(R.id.btnAdd);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void loadContacts() {
        contactList = databaseHelper.getAllContacts();
        adapter.updateData(contactList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContacts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contact_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_add) {
            Intent intent = new Intent(MainActivity.this, AddContactActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_sort) {
            adapter.sortByName();
            Toast.makeText(this, "Đã sắp xếp theo bảng chữ cái", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}