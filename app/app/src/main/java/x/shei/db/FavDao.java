package x.shei.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FavDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(FavEntity fav);

    @Query("SELECT * FROM fav")
    List<FavEntity> getAll();

    @Query("SELECT * FROM fav WHERE m3u8 = :m3u8 LIMIT 1")
    FavEntity findByM3u8(String m3u8);

    @Query("DELETE FROM fav WHERE m3u8 = :m3u8")
    void deleteByM3u8(String m3u8);
}
