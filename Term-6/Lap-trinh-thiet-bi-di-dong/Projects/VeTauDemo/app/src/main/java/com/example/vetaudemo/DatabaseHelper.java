package com.example.vetaudemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "VeTau.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_TICKET = "Ticket";
    public static final String COL_ID = "maVe";
    public static final String COL_GA_DI = "gaDi";
    public static final String COL_GA_DEN = "gaDen";
    public static final String COL_DON_GIA = "donGia";
    public static final String COL_LOAI_VE = "loaiVe";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_TICKET + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_GA_DI + " TEXT, " +
                COL_GA_DEN + " TEXT, " +
                COL_DON_GIA + " REAL, " +
                COL_LOAI_VE + " INTEGER)";
        db.execSQL(sql);

        insertSampleData(db);
    }

    private void insertSampleData(SQLiteDatabase db) {
        insertTicket(db, "Vinh", "Nam Định", 634.4575f, true);
        insertTicket(db, "Nam Định", "Thanh Hóa", 857.375f, true);
        insertTicket(db, "Hà Nội", "Nam Định", 473.271f, true);
        insertTicket(db, "Thanh Hóa", "Hà Nội", 170.000f, false);
        insertTicket(db, "Hà Nội", "Thanh Hóa", 170.000f, false);
    }

    private void insertTicket(SQLiteDatabase db, String gaDi, String gaDen, float donGia, boolean loaiVe) {
        ContentValues values = new ContentValues();
        values.put(COL_GA_DI, gaDi);
        values.put(COL_GA_DEN, gaDen);
        values.put(COL_DON_GIA, donGia);
        values.put(COL_LOAI_VE, loaiVe ? 1 : 0);
        db.insert(TABLE_TICKET, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TICKET);
        onCreate(db);
    }

    public long addTicket(Ticket ticket) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_GA_DI, ticket.getGaDi());
        values.put(COL_GA_DEN, ticket.getGaDen());
        values.put(COL_DON_GIA, ticket.getDonGia());
        values.put(COL_LOAI_VE, ticket.isLoaiVe() ? 1 : 0);
        return db.insert(TABLE_TICKET, null, values);
    }

    public int updateTicket(Ticket ticket) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_GA_DI, ticket.getGaDi());
        values.put(COL_GA_DEN, ticket.getGaDen());
        values.put(COL_DON_GIA, ticket.getDonGia());
        values.put(COL_LOAI_VE, ticket.isLoaiVe() ? 1 : 0);

        return db.update(TABLE_TICKET, values, COL_ID + "=?", new String[]{String.valueOf(ticket.getMaVe())});
    }

    public int deleteTicket(int maVe) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_TICKET, COL_ID + "=?", new String[]{String.valueOf(maVe)});
    }

    public ArrayList<Ticket> getAllTickets() {
        ArrayList<Ticket> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TICKET, null);

        if (cursor.moveToFirst()) {
            do {
                Ticket ticket = new Ticket();
                ticket.setMaVe(cursor.getInt(0));
                ticket.setGaDi(cursor.getString(1));
                ticket.setGaDen(cursor.getString(2));
                ticket.setDonGia(cursor.getFloat(3));
                ticket.setLoaiVe(cursor.getInt(4) == 1);
                list.add(ticket);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public ArrayList<Ticket> searchByGaDen(String keyword) {
        ArrayList<Ticket> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_TICKET + " WHERE " + COL_GA_DEN + " LIKE ?",
                new String[]{"%" + keyword + "%"}
        );

        if (cursor.moveToFirst()) {
            do {
                Ticket ticket = new Ticket();
                ticket.setMaVe(cursor.getInt(0));
                ticket.setGaDi(cursor.getString(1));
                ticket.setGaDen(cursor.getString(2));
                ticket.setDonGia(cursor.getFloat(3));
                ticket.setLoaiVe(cursor.getInt(4) == 1);
                list.add(ticket);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public Ticket getTicketById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_TICKET + " WHERE " + COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );

        Ticket ticket = null;
        if (cursor.moveToFirst()) {
            ticket = new Ticket(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getFloat(3),
                    cursor.getInt(4) == 1
            );
        }
        cursor.close();
        return ticket;
    }
}