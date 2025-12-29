package x.shei.db;

import android.content.Context;
import android.os.Environment;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Database(entities = {VideoEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase2 extends RoomDatabase {
    private static volatile AppDatabase2 INSTANCE;
    public static Context mContext;
    public abstract VideoDao videoDao();

    public static AppDatabase2 getInstance(Context context) {
        mContext = context;
        if (INSTANCE == null) {
            synchronized (AppDatabase2.class) {
                if (INSTANCE == null) {
                    File dbFile = new File(Environment.getExternalStorageDirectory(), "Download/db2/video_database");

                    if (!dbFile.getParentFile().exists()) {
                        dbFile.getParentFile().mkdirs();
                    }
//                    if (!dbFile.exists()){
//                        Log.e("asd","db文件不存在");
//                        copyDatabase(mContext);
//                    }

                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase2.class,
                            dbFile.getAbsolutePath()
                    ).build();
                }
            }
        }
        return INSTANCE;
    }

    private static final String DATABASE_NAME = "video_database";

    public static void copyDatabase(Context context) {
        // 检查外部存储是否可写
        if (!isExternalStorageWritable()) {
            return;
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            // 打开 assets 目录下的数据库文件
            inputStream = context.getAssets().open(DATABASE_NAME);

            // 获取外部存储路径
            File externalStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File dir = new File(externalStorageDir, "db2");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File outFile = new File(dir, DATABASE_NAME);

            // 将数据库文件复制到外部存储
            outputStream = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 检查外部存储是否可写
    private static boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

}
