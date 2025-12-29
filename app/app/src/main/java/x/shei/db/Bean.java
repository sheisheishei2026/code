package x.shei.db;

import java.io.Serializable;

public class Bean implements Serializable {
    public int id;
    public String src;
    public String a; //详情页
    public String a2; //播放页
    public String m3u8;
    public int type;
    public String title;
//    public String actor;

    // 新增：详情页字段
    public String director;    // 导演

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getA2() {
        return a2;
    }

    public void setA2(String a2) {
        this.a2 = a2;
    }

    public String getM3u8() {
        return m3u8;
    }

    public void setM3u8(String m3u8) {
        this.m3u8 = m3u8;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getMainActor() {
        return mainActor;
    }

    public void setMainActor(String mainActor) {
        this.mainActor = mainActor;
    }

    public String country;     // 国家/地区
    public String language;    // 语言
    public String updateTime;  // 更新时间
    public String intro;       // 详细介绍（完整内容）
    public String mainActor;   // 主演（详情页完整列表）


    @Override
    public String toString() {
        return "Bean{" +
                "id=" + id +
                ", src='" + src + '\'' +
                ", a='" + a + '\'' +
                ", a2='" + a2 + '\'' +
                ", m3u8='" + m3u8 + '\'' +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", director='" + director + '\'' +
                ", country='" + country + '\'' +
                ", language='" + language + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", intro='" + intro + '\'' +
                ", mainActor='" + mainActor + '\'' +
                '}';
    }
}
