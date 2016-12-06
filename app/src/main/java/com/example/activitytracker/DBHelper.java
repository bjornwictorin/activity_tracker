package com.example.activitytracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Björn on 2016-12-06.
 */

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory cf, int nbr) {
        super(context, name, cf, nbr);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE positions (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "latitude REAL, longitude REAL, altitude REAL, timestamp INTEGER");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS positions");
        onCreate(db);
    }
}
