package com.example.onthi1;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    SearchView searchView;
    ListView listView;

    DBHelper dbHelper;

    ArrayList<SinhVien> originalList;   // danh sách gốc từ DB
    ArrayList<SinhVien> displayList;    // danh sách đang hiển thị (sau khi search)
    SinhVienAdapter adapter;

    // id menu item context
    private static final int MENU_EDIT = 1;
    private static final int MENU_DELETE = 2;

    // item đang long press (nếu bạn muốn dùng cho Edit)
    private int selectedPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initWidget();
        initData();
        initSearch();
        initContextMenu();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initWidget() {
        searchView = findViewById(R.id.searchView);
        listView = findViewById(R.id.listView);
    }

    private void initData() {
        dbHelper = new DBHelper(this);

        originalList = dbHelper.getAllSinhVien();

        displayList = new ArrayList<>(originalList);

        adapter = new SinhVienAdapter(this, displayList);
        listView.setAdapter(adapter);
    }

    private void reloadFromDb() {
        originalList = dbHelper.getAllSinhVien();
        displayList.clear();
        displayList.addAll(originalList);
        adapter.notifyDataSetChanged();
    }

    // ===================== SEARCH THEO TÊN =====================
    private void initSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterByName(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterByName(newText);
                return true;
            }
        });
    }

    private void filterByName(String key) {
        String q = (key == null) ? "" : key.trim().toLowerCase();

        displayList.clear();

        if (q.isEmpty()) {
            displayList.addAll(originalList);
        } else {
            for (SinhVien sv : originalList) {
                if (sv.getTen() != null && sv.getTen().toLowerCase().contains(q)) {
                    displayList.add(sv);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    // ===================== CONTEXT MENU (EDIT/DELETE) =====================
    private void initContextMenu() {
        registerForContextMenu(listView);

        // lưu position đang chọn để Edit (nếu cần)
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            selectedPos = position;
            return false; // để ContextMenu vẫn hiện
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v.getId() == R.id.listView) {
            menu.setHeaderTitle("Chọn chức năng");
            menu.add(0, MENU_EDIT, 0, "Edit");
            menu.add(0, MENU_DELETE, 1, "Delete");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        // position trong danh sách đang hiển thị (displayList)
        int pos = info.position;

        if (item.getItemId() == MENU_EDIT) {
            // Bạn tự xử lý Edit theo UI của bạn (mở màn hình sửa, dialog sửa,...)
            SinhVien sv = displayList.get(pos);
            Toast.makeText(this, "Edit: " + sv.getTen(), Toast.LENGTH_SHORT).show();
            return true;
        }

        if (item.getItemId() == MENU_DELETE) {
            showConfirmDeleteLessThan25();
            return true;
        }

        return super.onContextItemSelected(item);
    }

    // ===================== DELETE: XOÁ TẤT CẢ SV TỔNG < 25 =====================
    private void showConfirmDeleteLessThan25() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có muốn xoá tất cả các học sinh có tổng điểm nhỏ hơn 25 không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    // gọi DB xoá
                    int deleted = dbHelper.deleteAllTongDiemLessThan25();

                    // load lại list
                    reloadFromDb();

                    // reset search (nếu muốn)
                    searchView.setQuery("", false);
                    searchView.clearFocus();

                    Toast.makeText(this, "Đã xoá " + deleted + " học sinh", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                .show();
    }
}