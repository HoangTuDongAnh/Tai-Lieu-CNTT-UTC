package com.example.contactadvance;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class MyDB extends SQLiteOpenHelper {
    //tên bảng
    public static final String TableName = "ContactTable3";
    //tên các cột trong bảng
    public static final String Id = "Id";
    public static final String Name = "Fullname";
    public static final String Phone = "Phonenumber";
    public static final String Img = "Image";
    public static final String Status = "Status";

    public MyDB(Context context, String name,
                SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //tạo câu sql để tạo bảng TableContact
        String sqlCreate = "Create table if not exists " + TableName + "("
                + Id + " Integer Primary key AUTOINCREMENT, "
                + Name + " Text, "
                + Phone + " Text, "
                + Img + " Text, "
                + Status + " Interger)";
        //chạy câu truy vấn SQL để tạo bảng
        db.execSQL(sqlCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //xóa bảng TableContact đã có
        db.execSQL("Drop table if exists " + TableName);
        //tạo lại
        onCreate(db);
    }

    //hàm thêm một contact vào bảng TableContact
    public void addContact(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put(Id, user.getId());
        value.put(Name, user.getName());
        value.put(Phone, user.getPhoneNum());
        value.put(Img, user.getImgUri());
        value.put(Status, user.getStatus() ? 1 : 0);
        db.insert(TableName, null, value);
        db.close();
    }

    public void updateContact(int id, User user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put(Id, user.getId());
        value.put(Name, user.getName());
        value.put(Phone, user.getPhoneNum());
        value.put(Img, user.getImgUri());
        value.put(Status, user.getStatus() ? 1 : 0);
        db.update(TableName, value, Id + "=?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteContact(int id) {
        SQLiteDatabase db = getWritableDatabase();
        //String sql = "Delete From " + TableName + " Where ID=" + id;
        db.delete(TableName, Id + "=?",new String[]{String.valueOf(id)});
        //db.execSQL(sql);
        db.close();
    }

    //lấy tất cả các dòng của bảng TableContact trả về dạng ArrayList
    public ArrayList<User> getAllContact() {
        ArrayList<User> list = new ArrayList<>();
        //câu truy vấn
        String sql = "Select * from " + TableName;
        //lấy đối tượng csdl sqlite
        SQLiteDatabase db = this.getReadableDatabase();
        //chạy câu truy vấn trả về dạng Cursor
        Cursor cursor = db.rawQuery(sql, null);
        //tạo ArrayList<Contact> để trả về;
        if (cursor != null)
            while (cursor.moveToNext()) {
                User user = new User(cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getInt(4) == 1);
                list.add(user);
            }
        return list;
    }
}


