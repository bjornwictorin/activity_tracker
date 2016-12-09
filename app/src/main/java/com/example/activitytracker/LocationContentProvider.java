package com.example.activitytracker;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by Björn on 2016-12-06.
 */

public class LocationContentProvider extends ContentProvider {

    private DBHelper dbHelper;
    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(LocationProviderContract.AUTHORITY, "locations", 1);
    }

    @Override
    public boolean onCreate() {
        this.dbHelper = new DBHelper(this.getContext(), "locationDB", null, 3);
        return true;
    }

    @Override
    public String getType(Uri uri) {
        if (uri.getLastPathSegment() == null) {
            return "vnd.android.cursor.dir/LocationContentProvider.data.text";
        } else {
            return "vnd.android.cursor.item/LocationContentProvider.data.text";
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String tableName;
        switch (uriMatcher.match(uri)) {
            case 1:
                tableName = "locations";
                break;
            default:
                tableName = "locations";
                break;
        }
        long id = db.insert(tableName, null, values);
        Uri nu = ContentUris.withAppendedId(uri, id);
        //Notify ContentObservers that the data in the db has changed.
        getContext().getContentResolver().notifyChange(nu, null);
        return nu;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String tableName;
        switch (uriMatcher.match(uri)) {
            case 1:
                tableName = "locations";
                break;
            default:
                tableName = "locations";
                break;
        }
        return db.query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String tableName;
        switch (uriMatcher.match(uri)) {
            case 1:
                tableName = "locations";
                break;
            default:
                tableName = "locations";
                break;
        }
        return db.update(tableName, values, selection, selectionArgs);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String tableName;
        switch (uriMatcher.match(uri)) {
            case 1:
                tableName = "locations";
                break;
            default:
                tableName = "locations";
                break;
        }
        return db.delete(tableName, selection, selectionArgs);
    }
}
