package x.shei.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "videos", indices = {@Index(value = {"m3u8"}, unique = true)})
public class VideoEntity {
    @PrimaryKey
    @NonNull
    public String m3u8;

    public String src;
    public String a2;
    public int type;

    public VideoEntity(String src, String m3u8, String a2, int type) {
        this.src = src;
        this.m3u8 = m3u8;
        this.a2 = a2;
        this.type = type;
    }
}
