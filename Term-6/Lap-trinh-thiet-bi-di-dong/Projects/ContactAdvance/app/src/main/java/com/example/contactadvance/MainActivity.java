package com.example.contactadvance;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
public class MainActivity extends AppCompatActivity {
    private ListView lvContact;
    private EditText etSearch;
    // MyDB mysqlitedb;
    //ArrayList chua du lieu cho listview
    ArrayList<String> Contacts;
    ArrayAdapter<String> lvAddapter;
    MyDB mysqlitedb;

    //để lưu dữ liệu danh sách các User
    //khai báo một ArrayList<User>
    ArrayList<User> listUser;
    //Adapter của listview hiển thị danh sách User
    MyAdapter listUserAdapter;
    int selectedid = -1;
    //    MyDB mysqlitedb;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private Button btnAdd, btnDel;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Tạo đối tượng phân tích menu layout
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.exmaple_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.mnuSort) {
            Toast.makeText(MainActivity.this, "Sort",
                    Toast.LENGTH_SHORT);
            Collections.sort(listUser, (u1, u2) -> {
                return u1.getName().toLowerCase().compareTo(u2.getName().toLowerCase());
            });
            listUserAdapter.notifyDataSetChanged();
            lvContact.setAdapter(listUserAdapter);
        } else if (itemId == R.id.mnuAdd) {//Tạo đối tượng Intent để gọi tới AddNew
            Intent intent = new Intent(MainActivity.this,
                    AddUser.class);
            startActivityForResult(intent, 100);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contextmenuitem, menu);

    }
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        User c = listUser.get(selectedid);
        int itemId = item.getItemId();
        if (itemId == R.id.mnuEdit) {//Tạo đối tượng Intent để gọi tới AddNew
            Intent intent = new Intent(this,
                    AddUser.class);
            Bundle bundle = new Bundle();
            bundle.putInt("Id", listUser.get(selectedid).getId());
            bundle.putString("Name", listUser.get(selectedid).getName());
            bundle.putString("Phone", listUser.get(selectedid).getPhoneNum());
            intent.putExtras(bundle);
            startActivityForResult(intent, 200);
            //Có thể thay thế bằng onActiityResult
        } else if (itemId == R.id.mnuDel) {
            Toast.makeText(this,
                    "Delete: " + listUser.get(selectedid).getName(),
                    Toast.LENGTH_SHORT).show();
            new AlertDialog.Builder(this).setTitle("Confirm Delete?")
                    .setMessage("Are you sure?")
                    .setPositiveButton("YES",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    mysqlitedb.deleteContact(listUser.get(selectedid).getId());
                                    listUser.remove(selectedid);
                                    listUserAdapter.notifyDataSetChanged();
                                    lvContact.setAdapter(listUserAdapter);
                                    dialog.dismiss();
                                }
                            })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();
        } else if (itemId == R.id.mnuCall) {
            Intent in = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + c.getPhoneNum()));
            startActivity(in);
        }
        return super.onContextItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 400) {
            return;
        }
        //lấy dữ liệu từ NewContact gửi về
        Bundle bundle = data.getExtras();
        int id = bundle.getInt("Id");
        String name = bundle.getString("Name");
        String phone = bundle.getString("Phone");
        if (requestCode == 100 && resultCode == 200) {
            //đặt vào listData
            listUser.add(new User(id, name, phone, "", false));
            mysqlitedb.addContact(new User(id, name, phone, "",false));
        } else if (requestCode == 200 && resultCode == 201){
            listUser.set(selectedid, new User(id, name, phone, "", false));
            mysqlitedb.updateContact(id,new User(id, name, phone, "", false));}
        //cập nhật adapter
        listUserAdapter.notifyDataSetChanged();
        lvContact.setAdapter(listUserAdapter);
    }

    private void handleButton() {
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Tạo đối tượng Intent để gọi tới AddNew
                Intent intent = new Intent(MainActivity.this,
                        AddUser.class);
                startActivityForResult(intent, 100);
            }
        });
        btnDel.setOnClickListener(view -> {
            // xoá user nếu giá trị kiểm tra của mỗi phần tử là true
            for (int i = 0; i < listUser.size(); i++) {
                if (listUser.get(i).getStatus()) {
                    listUser.remove(i).getId();
                    mysqlitedb.deleteContact(listUser.get(i).getId());
                }
            }
            listUser.removeIf(User::getStatus);
            listUserAdapter.notifyDataSetChanged();
        });
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //tham chiếu tới listview và editext
        lvContact = findViewById(R.id.lvContact);
        etSearch = findViewById(R.id.etSearch);
        btnAdd = findViewById(R.id.btnAdd);
        btnDel = findViewById(R.id.btnDel);

        //Toast.makeText(this, "adfa", Toast.LENGTH_SHORT).show();
        etSearch.setText("");
        registerForContextMenu(lvContact);
        //Broadcast Receiver
        initNetworkCallback();

        mysqlitedb = new MyDB(this, "ContactDB3", null, 3);
        mysqlitedb.addContact(new User(1, "Nam", "09893838", "", false));
        mysqlitedb.addContact(new User(2, "Bich", "03393039", "", false));
        mysqlitedb.addContact(new User(3, "Hai", "098789089", "", false));
        listUser = new ArrayList<>();
        listUser = mysqlitedb.getAllContact();
        //Tạo Adapter để đặt dữ liệu cho listview
        listUserAdapter = new MyAdapter(this, listUser);
        //Gắn Addapter vào cho listview
        lvContact.setAdapter(listUserAdapter);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                listUserAdapter.getFilter().filter(s.toString());
                listUserAdapter.notifyDataSetChanged();
                //lvContact.setAdapter(listUserAdapter);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        handleButton();

    }
    private void initNetworkCallback() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                Log.d("NetworkStatus", "ONLINE");
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Đã kết nối mạng", Toast.LENGTH_SHORT).show();
                });
            }
            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                Log.d("NetworkStatus", "OFFLINE");
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Mất kết nối mạng", Toast.LENGTH_SHORT).show();
                });
            }
        };


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            // Dùng registerDefaultNetworkCallback cho API 24+
//            connectivityManager.registerDefaultNetworkCallback(networkCallback);
//        } else {
//            // Dùng cách cũ hơn cho API 21-23
//            NetworkRequest request = new NetworkRequest.Builder()
//                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//                    .build();
//            connectivityManager.registerNetworkCallback(request, networkCallback);
//        }
    }

    //huy dang ki receiver
    @Override
    protected void onPause() {
        super.onPause();
        // Hủy đăng ký để tránh rò rỉ bộ nhớ
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    //return activity main
    @Override
    protected void onResume() {
        super.onResume();
        // Đăng ký lại
        initNetworkCallback();
    }
}
