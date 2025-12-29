package x.shei.db;

public class ListItem {
    private String title;
    private String imageUrl;
    private String link;

    public ListItem(String title, String imageUrl, String link ) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getLink() {
        return link;
    }

}
