package com.eduardorascon.luminarias.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.eduardorascon.luminarias.R;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "db1.db";
    private static DatabaseHandler dbInstance;
    private Context context;

    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
    }

    public static DatabaseHandler getInstance(Context context) {
        if (dbInstance == null)
            dbInstance = new DatabaseHandler(context);

        return dbInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LUMINARIAS_TABLE = context.getString(R.string.create_table_luminarias);
        db.execSQL(CREATE_LUMINARIAS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
