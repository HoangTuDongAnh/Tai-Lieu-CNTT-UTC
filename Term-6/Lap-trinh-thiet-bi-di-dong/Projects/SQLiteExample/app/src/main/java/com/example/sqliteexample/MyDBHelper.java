package com.example.sqliteexample;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MyDBHelper extends SQLiteOpenHelper {
    private static String DBName = "mydb.db";
    private static int VERSION = 1;

    private static String TABLE_NAME = "SinhVien";

    private static String ID = "_id";
    private static String NAME = "name";
    private static String YEAROB = "yearob";
    private SQLiteDatabase myDB;

    // Constructor
    public MyDBHelper(@Nullable Context context) {
        super(context, DBName, null, VERSION);
    }

    // Getter & Setter
    public static String getID() {
        return ID;
    }

    public static void setID(String ID) {
        MyDBHelper.ID = ID;
    }

    public static String getNAME() {
        return NAME;
    }

    public static void setNAME(String NAME) {
        MyDBHelper.NAME = NAME;
    }

    public static String getYEAROB() {
        return YEAROB;
    }

    public static void setYEAROB(String YEAROB) {
        MyDBHelper.YEAROB = YEAROB;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String queryTable = "CREATE TABLE " + TABLE_NAME + " (" +
                ID + " INTEGER PRIMARY KEY autoincrement, " +
                NAME + " TEXT NOT NULL, " +
                YEAROB + " INTEGER NOT NULL " +
                ")";
        sqLiteDatabase.execSQL(queryTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void openDB() {
        myDB = getWritableDatabase();
    }

    public void closeDB() {
        if (myDB != null && myDB.isOpen()) {
            myDB.close();
        }
    }

    // Truy van thong qua CURSOR - Thao tac thong qua ContentValues

    // Insert
    public long Insert(String name, int yearob) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(NAME, name);
        values.put(YEAROB, yearob);

        return db.insert(TABLE_NAME, null, values);
    }

    // Update
    public long Update(int id, String name, int yearob) {
        ContentValues values = new ContentValues();

        values.put(ID, id);
        values.put(NAME, name);
        values.put(YEAROB, yearob);

        String whereClause = ID + " = " + id;

        return myDB.update(TABLE_NAME, values, whereClause, null);
    }

    // Delete
    public long Delete(int id) {
        String whereClause = ID + " = " + id;

        return myDB.delete(TABLE_NAME, whereClause, null);
    }

    // Search
    public Cursor DisplayAll() {
        String query = "SELECT * FROM " + TABLE_NAME;

        return myDB.rawQuery(query, null);
    }

}
