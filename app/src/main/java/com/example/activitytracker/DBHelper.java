package com.example.activitytracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Björn on 2016-12-06.
 */

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory cf, int nbr) {
        super(context, name, cf, nbr);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("G53MDP", "DBHelper onCreate");
        db.execSQL("CREATE TABLE locations (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "latitude REAL, longitude REAL, altitude REAL, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("G53MDP", "DBHelper onUpgrade");
        db.execSQL("DROP TABLE IF EXISTS locations");
        onCreate(db);
    }
}
