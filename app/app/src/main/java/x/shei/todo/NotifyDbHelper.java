package x.shei.todo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class NotifyDbHelper {
    NotifyDb notifyDb = null;
    Context context;

    public NotifyDbHelper (Context context) {
        this.context = context;
        notifyDb = new NotifyDb (context);

    }

    //这个插入不用判重
    public void insert (NotifyTODO notifyTODO) {
        SQLiteDatabase database = null;
        try {
            database = notifyDb.getWritableDatabase ();
            ContentValues values = new ContentValues();
            values.put (NotifyDb.todoId, notifyTODO.getId ());
            values.put (NotifyDb.todoText, notifyTODO.getItem ());
            database.insert (NotifyDb.table, null, values);
        } catch (Exception e) {
            e.printStackTrace ();
        } finally {
            if (null != database) {
                database.close ();
            }
        }
    }


    // 删除某一条，防止sql注入攻击的写法
    public void delete (String todoId) {
        SQLiteDatabase database = null;
        try {
            database = notifyDb.getWritableDatabase ();
            database.delete (NotifyDb.table, NotifyDb.todoId + "=?", new String[]{todoId});
        } catch (Exception e) {
            e.printStackTrace ();
            Log.e ("log", "没有这一条");
        } finally {
            if (null != database) {
                database.close ();
            }
        }
    }


    // 删除某一条，防止sql注入攻击的写法
    public void deleteAll () {
        SQLiteDatabase database = null;
        try {
            database = notifyDb.getWritableDatabase ();
            database.delete (NotifyDb.table, null, null);
        } catch (Exception e) {
            e.printStackTrace ();
        } finally {
            if (null != database) {
                database.close ();
            }
        }
    }


    // 返回所有数据
    public NotifyTODO query (String todoId) {
        SQLiteDatabase database = null;
        NotifyTODO notifyTODO = null;
        try {
            database = notifyDb.getReadableDatabase ();
            Cursor cursor = database.query (NotifyDb.table, null, NotifyDb.todoId + "=?", new String[]{todoId}, null, null,
                    null, null);
            while (cursor.moveToNext ()) {
                //这里又重新new了一个，对吗，还有setId
                notifyTODO = new NotifyTODO ();
                notifyTODO.setId (todoId);
                notifyTODO.setItem (cursor.getString (cursor.getColumnIndex (NotifyDb.todoText)));
            }
        } catch (Exception e) {
            e.printStackTrace ();
        } finally {
            if (null != database) {
                database.close ();
            }
        }
        return notifyTODO;
    }

    // 返回所有数据
    public ArrayList<NotifyTODO> queryAll () {
        SQLiteDatabase database = null;
        ArrayList<NotifyTODO> arrayList = new ArrayList<NotifyTODO>();
        NotifyTODO notifyTODO = null;
        try {
            database = notifyDb.getReadableDatabase ();
            Cursor cursor = database.query (NotifyDb.table, null, null, null, null,
                    null, null);

            while (cursor.moveToNext ()) {
                notifyTODO = new NotifyTODO ();
                notifyTODO.setId (cursor.getString (cursor.getColumnIndex (NotifyDb.todoId)));
                notifyTODO.setItem (cursor.getString (cursor.getColumnIndex (NotifyDb.todoText)));
                arrayList.add (notifyTODO);
            }
        } catch (Exception e) {
            e.printStackTrace ();
        } finally {
            if (null != database) {
                database.close ();
            }
        }
        return arrayList;
    }


    //    // 是否存在
    //    public boolean isExit (String todoId) {
    //        SQLiteDatabase database = null;
    //        boolean result = false;
    //        try {
    //            database = notifyDb.getReadableDatabase ();
    //            Cursor cursor = database.query (NotifyDb.table, null, NotifyDb.todoId + "=?",
    //                    new String[]{todoId}, null, null, null);
    //            result = cursor.moveToNext ();
    //        } catch (Exception e) {
    //            e.printStackTrace ();
    //        } finally {
    //            if (null != database) {
    //                database.close ();
    //            }
    //        }
    //        return result;
    //    }


}
