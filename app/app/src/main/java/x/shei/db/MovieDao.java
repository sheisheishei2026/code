package x.shei.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface MovieDao {
    // 批量插入数据（若已存在则替换）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MovieEntity> movieEntities);

    // 查询所有数据
    @Query("SELECT * FROM movie")
    List<MovieEntity> getAllMovies();

    @Query("DELETE FROM movie WHERE dbId NOT IN (" +
            "SELECT MIN(dbId) FROM movie " + // 保留每组中 dbId 最小的记录（可改为 MAX 保留最新）
            "GROUP BY m3u8, title)")         // 按 m3u8 和 title 分组，删除重复数据
    void deleteDuplicates();

    // 根据 m3u8 和 title 删除指定记录
    @Query("DELETE FROM movie WHERE m3u8 = :m3u8 AND title = :title")
    void deleteByM3u8AndTitle(String m3u8, String title);

//    // 按导演查询（示例）
//    @Query("SELECT * FROM movie WHERE director = :director")
//    List<MovieEntity> getMoviesByDirector(String director);
}
