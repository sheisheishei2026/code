package x.shei.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface VideoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(VideoEntity video);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<VideoEntity> videos);

    @Query("SELECT * FROM videos WHERE m3u8 = :m3u8 LIMIT 1")
    VideoEntity findByM3u8(String m3u8);

    @Query("SELECT * FROM videos WHERE m3u8 = :src LIMIT 1")
    VideoEntity findBySrc(String src);

    @Query("SELECT * FROM videos WHERE type = :type")
    List<VideoEntity> getAllByType(int type);

    @Query("SELECT * FROM videos")
    List<VideoEntity> getAll();

}
