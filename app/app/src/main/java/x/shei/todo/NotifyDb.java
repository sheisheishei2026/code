package x.shei.todo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotifyDb extends SQLiteOpenHelper {
    public static String name = "notify.db";
    public static String table = "notifyTable";
    public static String todoId = "_todoId";
    public static String todoText = "_todoText";

    public static int version = 1;
    // 注意彼此的空格
    String sql = "CREATE TABLE " + table + "(" + todoId
            + " TEXT PRIMARY KEY," + todoText
            + " TEXT NOT NULL" + ")";

    public NotifyDb (Context context) {
        super (context, name, null, version);
    }

    @Override
    public void onCreate (SQLiteDatabase db) {
        db.execSQL (sql);

    }

    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            db.execSQL (sql);
        }

    }

}
