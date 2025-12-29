package x.shei.db.movie;

import android.content.Context;
import android.os.Environment;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.io.File;

@Database(
    entities = {MovieEntity.class, MovieDownload.class},
    version = 1,
    exportSchema = false
)
public abstract class MovieDatabase extends RoomDatabase {
    private static volatile MovieDatabase INSTANCE;

    public abstract MovieDao movieDao();

    public static MovieDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (MovieDatabase.class) {
                if (INSTANCE == null) {
                    File dbFile = new File(Environment.getExternalStorageDirectory(),
                        "Download/db/movie.db");

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
