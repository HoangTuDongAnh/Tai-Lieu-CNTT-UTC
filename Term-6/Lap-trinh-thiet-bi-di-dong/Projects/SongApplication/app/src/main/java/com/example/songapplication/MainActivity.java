package com.example.songapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText edtSearch, edtSongName, edtSinger, edtRating;
    Button btnAdd, btnUpdate;
    RecyclerView rvSong;

    DatabaseHelper databaseHelper;
    SongAdapter adapter;
    ArrayList<Song> songList;

    Song selectedSong = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        anhXa();

        databaseHelper = new DatabaseHelper(this);

        songList = databaseHelper.getAllSongs();
        adapter = new SongAdapter(songList, song -> {
            selectedSong = song;
            edtSongName.setText(song.getName());
            edtSinger.setText(song.getSinger());
            edtRating.setText(String.valueOf(song.getRating()));
        });

        rvSong.setLayoutManager(new LinearLayoutManager(this));
        rvSong.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> addSong());

        btnUpdate.setOnClickListener(v -> updateSong());

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchSong(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        Button btnOpenRingtone = findViewById(R.id.btnOpenRingtone);

        btnOpenRingtone.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RingtoneActivity.class);
            startActivity(intent);
        });
    }

    private void anhXa() {
        edtSearch = findViewById(R.id.edtSearch);
        edtSongName = findViewById(R.id.edtSongName);
        edtSinger = findViewById(R.id.edtSinger);
        edtRating = findViewById(R.id.edtRating);
        btnAdd = findViewById(R.id.btnAdd);
        btnUpdate = findViewById(R.id.btnUpdate);
        rvSong = findViewById(R.id.rvSong);
    }

    private void addSong() {
        String name = edtSongName.getText().toString().trim();
        String singer = edtSinger.getText().toString().trim();
        String ratingStr = edtRating.getText().toString().trim();

        if (name.isEmpty() || singer.isEmpty() || ratingStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        float rating;
        try {
            rating = Float.parseFloat(ratingStr);
        } catch (Exception e) {
            Toast.makeText(this, "Điểm đánh giá không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Song song = new Song(name, rating, singer);
        boolean result = databaseHelper.insertSong(song);

        if (result) {
            Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
            clearInput();
            loadData();
        } else {
            Toast.makeText(this, "Thêm thất bại", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSong() {
        if (selectedSong == null) {
            Toast.makeText(this, "Vui lòng chọn bài hát cần sửa", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = edtSongName.getText().toString().trim();
        String singer = edtSinger.getText().toString().trim();
        String ratingStr = edtRating.getText().toString().trim();

        if (name.isEmpty() || singer.isEmpty() || ratingStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        float rating;
        try {
            rating = Float.parseFloat(ratingStr);
        } catch (Exception e) {
            Toast.makeText(this, "Điểm đánh giá không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedSong.setName(name);
        selectedSong.setSinger(singer);
        selectedSong.setRating(rating);

        boolean result = databaseHelper.updateSong(selectedSong);

        if (result) {
            Toast.makeText(this, "Sửa thành công", Toast.LENGTH_SHORT).show();
            clearInput();
            selectedSong = null;
            loadData();
        } else {
            Toast.makeText(this, "Sửa thất bại", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchSong(String keyword) {
        ArrayList<Song> list;
        if (keyword.isEmpty()) {
            list = databaseHelper.getAllSongs();
        } else {
            list = databaseHelper.searchSongByName(keyword);
        }
        adapter.setSongList(list);
    }

    private void loadData() {
        songList = databaseHelper.getAllSongs();
        adapter.setSongList(songList);
    }

    private void clearInput() {
        edtSongName.setText("");
        edtSinger.setText("");
        edtRating.setText("");
    }
}