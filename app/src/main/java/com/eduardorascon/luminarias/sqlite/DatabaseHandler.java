package com.eduardorascon.luminarias.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.eduardorascon.luminarias.R;

import java.util.ArrayList;
import java.util.List;

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

    public long insertLuminaria(Luminaria luminaria) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("lat", luminaria.getLat());
        contentValues.put("lon", luminaria.getLon());
        contentValues.put("tipo_poste", luminaria.getTipoPoste());
        contentValues.put("tipo_lampara", luminaria.getTipoLampara());
        contentValues.put("altura", luminaria.getAltura());
        contentValues.put("nombre_imagen", luminaria.getImagen());
        return db.insert("luminarias", null, contentValues);
    }

    public List<Luminaria> getAllLuminarias() {
        List<Luminaria> luminariasList = new ArrayList<>();
        String selectAll = "SELECT lat, lon, tipo_poste, tipo_lampara, altura, nombre_imagen, fecha_hora FROM luminarias";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectAll, null);

        if (cursor.moveToFirst() == false)
            return luminariasList;

        do {
            Luminaria l = new Luminaria();
            l.setLat(cursor.getString(0));//lat
            l.setLon(cursor.getString(1));//lon
            l.setTipoPoste(cursor.getString(2));//tipo_poste
            l.setTipoLampara(cursor.getString(3));//tipo_lampara
            l.setAltura(cursor.getString(4));//altura
            l.setImagen(cursor.getString(5));//nombre_imagen
            l.setFechaHora(cursor.getString(6));//fecha_hora
        } while (cursor.moveToNext());

        return luminariasList;
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
