package com.example.chat.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    final String LOG_TAG = "dbLogs";
    public DBHelper(Context context) {
        super(context, "myDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "--- onCreate database ---");
        db.execSQL(
                "create table users (" +
                        "id integer primary key autoincrement," +
                        "name text);"
        );
        db.execSQL(
                "create table companions (" +
                        "id integer primary key autoincrement," +
                        "name text," +
                        "deviceCode text);"
        );
        db.execSQL(
                "CREATE TABLE messages (" +
                        "companionsDevice text," +
                        "companionMessage text," +
                        "myMessage text," +
                "FOREIGN KEY(companionsDevice) REFERENCES companions(deviceCode));"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public ArrayList<String> getAllUsers(DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ArrayList<String> users = new ArrayList<>();

        Cursor cursor = db.query("users", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                users.add(cursor.getString(1));
            } while (cursor.moveToNext());
        } else {
            Log.d(LOG_TAG, "0 rows");
        }

        dbHelper.close();
        return users;
    }

    public void insertNewUser(DBHelper dbHelper, String username) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", username);
        db.insert("users", null, cv);
        dbHelper.close();
    }

    public void delete(DBHelper dbHelper, String username) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] string = new String[] {username};
        db.delete("users", "name = ?", string);
        dbHelper.close();
    }

    public int checkCompanion(DBHelper dbHelper, String where) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        String[] whereSQL = {where};
        Cursor cursor = database.query("companions", new String[] {"id"}, "deviceCode = ?", whereSQL, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int id = cursor.getInt(0);
            dbHelper.close();
            return id;
        } else {
            dbHelper.close();
            return 0;
        }
    }


    public int addCompanion(DBHelper dbHelper, String name, String deviceAddress) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("deviceCode", deviceAddress);
        db.insert("companions", null, cv);
        dbHelper.close();

        return checkCompanion(dbHelper, deviceAddress);
    }

    public void getMessages(DBHelper dbHelper, int companionId) {

    }
}
