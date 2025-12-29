package x.shei.db;

import android.content.Context;
import android.os.Environment;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.io.File;

@Database(entities = {FavEntity.class}, version = 1, exportSchema = false)
public abstract class FavDatabase extends RoomDatabase {
    private static volatile FavDatabase INSTANCE;

    public abstract FavDao favDao();

    public static FavDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (FavDatabase.class) {
                if (INSTANCE == null) {
                    File dbFile = new File(Environment.getExternalStorageDirectory(), "Download/db/db");

                    if (!dbFile.getParentFile().exists()) {
                        dbFile.getParentFile().mkdirs();
                    }

                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            FavDatabase.class,
                            dbFile.getAbsolutePath()
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
