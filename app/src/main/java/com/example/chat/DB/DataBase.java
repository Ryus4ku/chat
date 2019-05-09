package com.example.chat.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.chat.Chat.Message;

import java.util.ArrayList;

public class DataBase extends SQLiteOpenHelper {
    final String LOG_TAG = "dbLogs";
    public static final String COMPANIONS_TABLE = "companions";
    public static final String USERS_TABLE = "users";

    public DataBase(Context context) {
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
                        "name text);"
        );
        db.execSQL(
                "CREATE TABLE messages (" +
                        "idCompanion integer," +
                        "idUser integer," +
                        "owned integer," +
                        "message text," +
                        "time text," +
                "FOREIGN KEY (idCompanion) REFERENCES companions(id)," +
                "FOREIGN KEY (idUser)      REFERENCES users(id));"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public ArrayList<String> getAllUsers(DataBase dataBase) {
        SQLiteDatabase db = dataBase.getWritableDatabase();
        ArrayList<String> users = new ArrayList<>();

        Cursor cursor = db.query("users", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                users.add(cursor.getString(1));
            } while (cursor.moveToNext());
        } else {
            Log.d(LOG_TAG, "0 rows");
        }

        dataBase.close();
        return users;
    }

    public void insertNewUser(DataBase dataBase, String username) {
        SQLiteDatabase db = dataBase.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", username);
        db.insert("users", null, cv);
        dataBase.close();
    }

    public void delete(DataBase dataBase, String username) {
        SQLiteDatabase db = dataBase.getWritableDatabase();
        String[] string = new String[] {username};
        db.delete("users", "name = ?", string);
        dataBase.close();
    }

    public int getIdByNameFromCustomTable(DataBase dataBase, String table, String name) {
        SQLiteDatabase database = dataBase.getWritableDatabase();
        String[] nameForSQL = {name};
        Cursor cursor = database.query(table, new String[] {"id"}, "name = ?", nameForSQL, null, null, null);
        int id = 0;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            id = cursor.getInt(0);
        }

        dataBase.close();
        return id;
    }

    public int addCompanion(DataBase dataBase, String name) {
        SQLiteDatabase db = dataBase.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        db.insert(COMPANIONS_TABLE, null, cv);
        dataBase.close();

        return getIdByNameFromCustomTable(dataBase, COMPANIONS_TABLE, name);
    }

    public void addMessage(DataBase dataBase, boolean owned, int companionId, int userId, String message, String time){
        SQLiteDatabase db = dataBase.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("idCompanion", companionId);
        cv.put("idUser", userId);
        cv.put("owned", owned ? 1 : 0);
        cv.put("message", message);
        cv.put("time", time);
        db.insert("messages", null, cv);
        dataBase.close();
    }

    public ArrayList<Message> getMessages(DataBase dataBase, int companionId, int userId) {
        SQLiteDatabase db = dataBase.getWritableDatabase();
        ArrayList<Message> messages = new ArrayList<>();
        String[] whereSQL = {String.valueOf(companionId), String.valueOf(userId)};
        Cursor cursor = db.query("messages", new String[] {"owned, message, time"}, "idCompanion = ? and idUser = ?", whereSQL, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Message message = new Message(cursor.getString(1), cursor.getInt(0) == 1, null, cursor.getString(2));
                messages.add(message);
            } while (cursor.moveToNext());
        } else {
            Log.d(LOG_TAG, "0 rows");
        }

        dataBase.close();
        return messages;
    }
}
