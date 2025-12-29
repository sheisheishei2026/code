package x.shei.db.movie;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface MovieDao {
    @Transaction
    @Query("SELECT * FROM movie")
    List<MovieWithDownloads> getAllMoviesWithDownloads();
}
