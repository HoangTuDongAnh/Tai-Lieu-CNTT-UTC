package com.example.songapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "song_db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_SONG = "songs";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_RATING = "rating";
    private static final String COL_SINGER = "singer";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_SONG + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT, "
                + COL_RATING + " REAL, "
                + COL_SINGER + " TEXT)";
        db.execSQL(createTable);

        // dữ liệu mẫu
        db.execSQL("INSERT INTO " + TABLE_SONG + " (name, rating, singer) VALUES ('Kiếp đỏ đen', 4.56, 'Duy Mạnh')");
        db.execSQL("INSERT INTO " + TABLE_SONG + " (name, rating, singer) VALUES ('Xuân này con không về', 5.2, 'Quang Lê')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONG);
        onCreate(db);
    }

    public boolean insertSong(Song song) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, song.getName());
        values.put(COL_RATING, song.getRating());
        values.put(COL_SINGER, song.getSinger());

        long result = db.insert(TABLE_SONG, null, values);
        return result != -1;
    }

    public boolean updateSong(Song song) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, song.getName());
        values.put(COL_RATING, song.getRating());
        values.put(COL_SINGER, song.getSinger());

        int result = db.update(TABLE_SONG, values, COL_ID + "=?", new String[]{String.valueOf(song.getId())});
        return result > 0;
    }

    public ArrayList<Song> getAllSongs() {
        ArrayList<Song> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SONG, null);

        if (cursor.moveToFirst()) {
            do {
                Song song = new Song();
                song.setId(cursor.getInt(0));
                song.setName(cursor.getString(1));
                song.setRating(cursor.getFloat(2));
                song.setSinger(cursor.getString(3));
                list.add(song);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    public ArrayList<Song> searchSongByName(String keyword) {
        ArrayList<Song> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_SONG + " WHERE " + COL_NAME + " LIKE ?",
                new String[]{"%" + keyword + "%"}
        );

        if (cursor.moveToFirst()) {
            do {
                Song song = new Song();
                song.setId(cursor.getInt(0));
                song.setName(cursor.getString(1));
                song.setRating(cursor.getFloat(2));
                song.setSinger(cursor.getString(3));
                list.add(song);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }
}