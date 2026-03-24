package com.example.nhahangdemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "NhaHangDB.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "NhaHang";
    public static final String COL_ID = "maNhaHang";
    public static final String COL_TEN = "tenNhaHang";
    public static final String COL_DIACHI = "diaChi";
    public static final String COL_DANHGIA = "danhGia";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlCreate = "CREATE TABLE " + TABLE_NAME + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TEN + " TEXT, "
                + COL_DIACHI + " TEXT, "
                + COL_DANHGIA + " REAL)";
        db.execSQL(sqlCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long insertNhaHang(NhaHang nhaHang) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TEN, nhaHang.getTenNhaHang());
        values.put(COL_DIACHI, nhaHang.getDiaChi());
        values.put(COL_DANHGIA, nhaHang.getDanhGia());
        return db.insert(TABLE_NAME, null, values);
    }

    public ArrayList<NhaHang> getAllNhaHang() {
        ArrayList<NhaHang> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                int ma = cursor.getInt(0);
                String ten = cursor.getString(1);
                String diaChi = cursor.getString(2);
                double danhGia = cursor.getDouble(3);

                list.add(new NhaHang(ma, ten, diaChi, danhGia));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    public void deleteAllNhaHang() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    public int deleteNhaHangBelowRating(double rating) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, COL_DANHGIA + " < ?", new String[]{String.valueOf(rating)});
    }

    public void insertSampleData() {
        insertNhaHang(new NhaHang("Sen Tây Hồ", "514 Lạc Long Quân", 8.6));
        insertNhaHang(new NhaHang("Nón Lá", "Nguyễn Đình Chiểu", 8.8));
        insertNhaHang(new NhaHang("Quán Ngon Hà Nội", "Phan Bội Châu", 8.9));
        insertNhaHang(new NhaHang("Lục Thủy", "Lê Thái Tổ", 8.5));
        insertNhaHang(new NhaHang("Charm Cham", "Phan Văn Chương", 8.2));
        insertNhaHang(new NhaHang("Ly Club", "Lê Phụng Hiếu", 7.8));
    }
}