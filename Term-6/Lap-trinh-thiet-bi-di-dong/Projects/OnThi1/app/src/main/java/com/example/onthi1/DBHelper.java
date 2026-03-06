package com.example.onthi1;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "SinhVienDB";
    public static final String TABLE_NAME = "SinhVien";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE SinhVien(" +
                "maSV INTEGER PRIMARY KEY," +
                "ten TEXT," +
                "diemToan INTEGER," +
                "diemHoa INTEGER," +
                "diemLy INTEGER)";

        db.execSQL(sql);

        // dữ liệu mẫu
        db.execSQL("INSERT INTO SinhVien VALUES(1,'Hoang',8,7,9)");
        db.execSQL("INSERT INTO SinhVien VALUES(2,'Ha',6,5,7)");
        db.execSQL("INSERT INTO SinhVien VALUES(3,'Hieu',9,8,9)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public ArrayList<SinhVien> getAllSinhVien(){

        ArrayList<SinhVien> list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM SinhVien", null);

        while(cursor.moveToNext()){

            int ma = cursor.getInt(0);
            String ten = cursor.getString(1);
            int toan = cursor.getInt(2);
            int hoa = cursor.getInt(3);
            int ly = cursor.getInt(4);

            SinhVien sv = new SinhVien(ma,ten,toan,hoa,ly);

            list.add(sv);
        }

        return list;
    }

    public int deleteAllTongDiemLessThan25() {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE (diemToan + diemHoa + diemLy) < 25",
                null
        );
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();

        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE (diemToan + diemHoa + diemLy) < 25");

        return count;
    }
}
