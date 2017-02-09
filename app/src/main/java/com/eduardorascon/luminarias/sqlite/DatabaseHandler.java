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

    public long updateLuminariaRespaldoDatos(Luminaria luminaria) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("respaldo_datos", 1);
        return db.update("luminarias", contentValues, "_id=?", new String[]{String.valueOf(luminaria.getId())});
    }

    public long updateLuminariaRespladoImagen(Luminaria luminaria) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("respaldo_imagen", 1);
        return db.update("luminarias", contentValues, "_id=?", new String[]{String.valueOf(luminaria.getId())});
    }

    public List<Luminaria> getAllLuminarias() {
        List<Luminaria> luminariasList = new ArrayList<>();
        String selectAll = "SELECT _id, lat, lon, tipo_poste, tipo_lampara, altura, nombre_imagen, fecha_hora, respaldo_imagen FROM luminarias where respaldo_datos = 0";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectAll, null);

        if (cursor.moveToFirst() == false)
            return luminariasList;

        do {
            Luminaria l = new Luminaria();
            l.setId(cursor.getInt(0));//id
            l.setLat(cursor.getString(1));//lat
            l.setLon(cursor.getString(2));//lon
            l.setTipoPoste(cursor.getString(3));//tipo_poste
            l.setTipoLampara(cursor.getString(4));//tipo_lampara
            l.setAltura(cursor.getString(5));//altura
            l.setImagen(cursor.getString(6));//nombre_imagen
            l.setFechaHora(cursor.getString(7));//fecha_hora
            l.setRespaldoImagen(cursor.getInt(8));//respaldo_imagen
            luminariasList.add(l);
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
