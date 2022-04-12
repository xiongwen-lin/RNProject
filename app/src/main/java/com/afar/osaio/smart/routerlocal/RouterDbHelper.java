package com.afar.osaio.smart.routerlocal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class RouterDbHelper extends SQLiteOpenHelper {

    private static final String CREATE_ROUTER = "create table router_num("
            +"id integer primary key autoincrement,"
            +"name text,"
            +"mac text,"
            +"isbind text)";

    public RouterDbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_ROUTER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
