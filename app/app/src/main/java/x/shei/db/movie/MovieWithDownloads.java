package x.shei.db.movie;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class MovieWithDownloads {
    @Embedded
    public MovieEntity movie;

    @Relation(
        parentColumn = "id",
        entityColumn = "movie_id"
    )
    public List<MovieDownload> downloads;
}
