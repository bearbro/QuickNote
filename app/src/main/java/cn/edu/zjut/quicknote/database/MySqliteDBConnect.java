package cn.edu.zjut.quicknote.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class MySqliteDBConnect extends SQLiteOpenHelper {
    private String CREATE_NOTE="create table Note("
            +"noteId Integer primary key autoincrement,"
            +"createdTime Integer,"
            +"modifiedTime Integer,"
            +"noteContent text," +
            "path text)";

    public MySqliteDBConnect(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context,name,factory,version);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_NOTE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
