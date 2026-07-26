package com.example.de260206;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "DonHangDB";
    public static final int DB_VERSION = 1;
    public static final String TABLE_NAME = "DonHang";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + "(" +
                "ma TEXT PRIMARY KEY," +
                "tenHang TEXT," +
                "ngayDat TEXT," +
                "giaHang REAL," +
                "giaoNhanh INTEGER)";
        db.execSQL(sql);
        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    private void insertSampleData(SQLiteDatabase db) {
        // Ma bat dau tu B = 17, buoc nhay C = 5: DH17, DH22, DH27, DH32
        db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES('DH17','Nước giặt','2023-01-15',1000000,0)");
        db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES('DH22','Quạt','2023-01-18',1500000,1)");
        db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES('DH27','Áo thun','2023-01-22',800000,1)");
        db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES('DH32','Bình hoa','2023-01-17',300000,0)");
    }

    public ArrayList<DonHang> getAllDonHang() {
        return getDonHangByQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public ArrayList<DonHang> getDonHangFromDate(String fromDate) {
        return getDonHangByQuery(
                "SELECT * FROM " + TABLE_NAME + " WHERE ngayDat >= ? ORDER BY ngayDat ASC",
                new String[]{fromDate}
        );
    }

    private ArrayList<DonHang> getDonHangByQuery(String sql, String[] args) {
        ArrayList<DonHang> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, args);

        while (cursor.moveToNext()) {
            String ma = cursor.getString(0);
            String tenHang = cursor.getString(1);
            String ngayDat = cursor.getString(2);
            float giaHang = cursor.getFloat(3);
            boolean giaoNhanh = cursor.getInt(4) == 1;
            list.add(new DonHang(ma, tenHang, ngayDat, giaHang, giaoNhanh));
        }

        cursor.close();
        return list;
    }
}
