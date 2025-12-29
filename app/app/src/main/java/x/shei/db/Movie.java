package x.shei.db;

public class Movie {
    private String title;       // 影片标题（如“女佣”）
    private String picUrl;      // 图片完整链接（如“https://www.sh-yxfhm.com/upload/vod/xxx.jpg”）
    private String detailUrl;   // 详情页完整链接（如“https://www.sh-yxfhm.com/news/34224.html”）

    // 构造方法、Getter、Setter
    public Movie(String title, String picUrl, String detailUrl) {
        this.title = title;
        this.picUrl = picUrl;
        this.detailUrl = detailUrl;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPicUrl() { return picUrl; }
    public void setPicUrl(String picUrl) { this.picUrl = picUrl; }
    public String getDetailUrl() { return detailUrl; }
    public void setDetailUrl(String detailUrl) { this.detailUrl = detailUrl; }

    // 用于日志打印查看数据
    @Override
    public String toString() {
        return "Movie{" +
                "title='" + title + '\'' +
                ", picUrl='" + picUrl + '\'' +
                ", detailUrl='" + detailUrl + '\'' +
                '}';
    }
}
