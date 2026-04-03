package com.example.vetaudemo;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView lvTicket;
    private SearchView searchView;
    private FloatingActionButton fabAdd;

    private DatabaseHelper dbHelper;
    private ArrayList<Ticket> ticketList;
    private TicketAdapter adapter;

    private WifiReceiver wifiReceiver;
    private boolean isReceiverRegistered = false;

    private final ActivityResultLauncher<Intent> launcher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> loadData());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lvTicket = findViewById(R.id.lvTicket);
        searchView = findViewById(R.id.searchView);
        fabAdd = findViewById(R.id.fabAdd);

        dbHelper = new DatabaseHelper(this);
        ticketList = dbHelper.getAllTickets();
        adapter = new TicketAdapter(this, ticketList);
        lvTicket.setAdapter(adapter);

        wifiReceiver = new WifiReceiver();

        setupSearchView();
        setupFab();
        setupLongClickMenu();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!isReceiverRegistered) {
            IntentFilter filter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
            registerReceiver(wifiReceiver, filter);
            isReceiverRegistered = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (isReceiverRegistered) {
            unregisterReceiver(wifiReceiver);
            isReceiverRegistered = false;
        }
    }

    private void setupSearchView() {
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();
        searchView.setQueryHint("Tìm theo ga đến");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchTicket(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchTicket(newText);
                return true;
            }
        });
    }

    private void setupFab() {
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditTicketActivity.class);
            launcher.launch(intent);
        });
    }

    private void setupLongClickMenu() {
        lvTicket.setOnItemLongClickListener((parent, view, position, id) -> {
            Ticket selectedTicket = (Ticket) adapter.getItem(position);
            showPopupMenu(view, selectedTicket);
            return true;
        });
    }

    private void searchTicket(String keyword) {
        ArrayList<Ticket> result;

        if (keyword == null || keyword.trim().isEmpty()) {
            result = dbHelper.getAllTickets();
        } else {
            result = dbHelper.searchByGaDen(keyword.trim());
        }

        adapter.setData(result);
    }

    private void showPopupMenu(View view, Ticket ticket) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> handleMenuClick(item, ticket));
        popupMenu.show();
    }

    private boolean handleMenuClick(MenuItem item, Ticket ticket) {
        if (item.getItemId() == R.id.menu_edit) {
            Intent intent = new Intent(MainActivity.this, AddEditTicketActivity.class);
            intent.putExtra("id", ticket.getMaVe());
            launcher.launch(intent);
            return true;
        } else if (item.getItemId() == R.id.menu_delete) {
            int row = dbHelper.deleteTicket(ticket.getMaVe());
            if (row > 0) {
                loadData();
                Toast.makeText(this, "Đã xóa vé", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return false;
    }

    private void loadData() {
        ticketList = dbHelper.getAllTickets();
        adapter.setData(ticketList);
    }
}