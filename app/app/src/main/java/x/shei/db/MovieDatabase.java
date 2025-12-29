package x.shei.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import android.os.Environment;

import java.io.File;

@Database(entities = {MovieEntity.class}, version = 1, exportSchema = false)
public abstract class MovieDatabase extends RoomDatabase {
    // 单例模式（避免重复创建数据库）
    private static volatile MovieDatabase INSTANCE;

    public abstract MovieDao movieDao();

    public static MovieDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (MovieDatabase.class) {
                if (INSTANCE == null) {
                    File dbFile = new File(Environment.getExternalStorageDirectory(),
                            "Download/movie.db");
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            MovieDatabase.class,
                            dbFile.getAbsolutePath()
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
