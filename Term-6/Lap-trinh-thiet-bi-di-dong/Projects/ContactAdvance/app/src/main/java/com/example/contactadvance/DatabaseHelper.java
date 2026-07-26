package com.example.contactadvance.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.contactadvance.models.Contact;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "contact_advance.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_CONTACT = "contacts";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_PHONE = "phone";
    public static final String COL_IMAGE = "image";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_CONTACT + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT, "
                + COL_PHONE + " TEXT, "
                + COL_IMAGE + " TEXT)";
        db.execSQL(createTable);

        insertSampleData(db);
    }

    private void insertSampleData(SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        values.put(COL_NAME, "Nam");
        values.put(COL_PHONE, "09898388");
        values.put(COL_IMAGE, "");
        db.insert(TABLE_CONTACT, null, values);

        values.clear();
        values.put(COL_NAME, "Bich");
        values.put(COL_PHONE, "03393039");
        values.put(COL_IMAGE, "");
        db.insert(TABLE_CONTACT, null, values);

        values.clear();
        values.put(COL_NAME, "Hai");
        values.put(COL_PHONE, "098789089");
        values.put(COL_IMAGE, "");
        db.insert(TABLE_CONTACT, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT);
        onCreate(db);
    }

    public long addContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_NAME, contact.getName());
        values.put(COL_PHONE, contact.getPhone());
        values.put(COL_IMAGE, contact.getImage());

        return db.insert(TABLE_CONTACT, null, values);
    }

    public List<Contact> getAllContacts() {
        List<Contact> contactList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CONTACT, null);

        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                contact.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
                contact.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
                contact.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE)));
                contact.setImage(cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE)));
                contact.setChecked(false);

                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return contactList;
    }

    public List<Contact> searchContactsByName(String keyword) {
        List<Contact> contactList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_CONTACT + " WHERE " + COL_NAME + " LIKE ?",
                new String[]{"%" + keyword + "%"}
        );

        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                contact.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
                contact.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
                contact.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE)));
                contact.setImage(cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE)));
                contact.setChecked(false);

                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return contactList;
    }

    public void deleteContactById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACT, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void deleteSelectedContacts(List<Contact> contacts) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (Contact contact : contacts) {
            if (contact.isChecked()) {
                db.delete(TABLE_CONTACT, COL_ID + "=?", new String[]{String.valueOf(contact.getId())});
            }
        }
    }
}