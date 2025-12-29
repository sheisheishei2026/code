package x.shei.db.movie;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "movie")
public class MovieEntity {
    @PrimaryKey
    public long id;

    public String area;
    public String img;
    public String title;
    public String content;
    public String contentHtml;
    public String actor;
}
