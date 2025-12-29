package x.shei.db.movie;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
    tableName = "movie$download",
    primaryKeys = {"movie_id", "name"},
    foreignKeys = @ForeignKey(
        entity = MovieEntity.class,
        parentColumns = "id",
        childColumns = "movie_id"
    ),
    indices = @Index("movie_id")
)
public class MovieDownload {
    public long movie_id;

    @NonNull
    public String name;

    public String url;
}
