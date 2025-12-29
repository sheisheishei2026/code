package x.shei.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// 数据库表名：movie
@Entity(tableName = "movie")
public class MovieEntity {
    @PrimaryKey(autoGenerate = true) // 自增 ID
    public int dbId;

    // 与 Bean 字段一一对应（可直接从 Bean 复制）
    public String title;
    public String director;
    public String mainActor;
    public String country;
    public String updateTime;
    public String intro;
    public String src;
    public String a;
    public String a2;
    public String m3u8;

    // 构造方法（从 Bean 转换为 MovieEntity）
    public MovieEntity(Bean bean) {
        this.title = bean.getTitle();
        this.director = bean.getDirector();
        this.mainActor = bean.getMainActor();
        this.country = bean.getCountry();
        this.updateTime = bean.getUpdateTime();
        this.intro = bean.getIntro();
        this.src = bean.getSrc();
        this.a = bean.getA();
        this.a2 = bean.getA2();
        this.m3u8 = bean.getM3u8();
    }

    public MovieEntity() {
    }
    public Bean rec() {
        Bean bean =new Bean();
        bean.title = title;
        bean.director = director;
        bean.mainActor = mainActor;
        bean.country = country;
        bean.updateTime = updateTime;
        bean.intro = intro;
        bean.src = src;
        bean.a = a;
        bean.a2 = a2;
        bean.m3u8 = m3u8;
        return bean;
    }
}
