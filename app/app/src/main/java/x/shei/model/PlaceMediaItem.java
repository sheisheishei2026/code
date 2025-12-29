package x.shei.model;

public class PlaceMediaItem {
    private String url;
    private String thumbnailUrl;
    private boolean isVideo;

    public PlaceMediaItem(String url, boolean isVideo) {
        this.url = url;
        this.isVideo = isVideo;
    }

    public PlaceMediaItem(String url, String thumbnailUrl, boolean isVideo) {
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.isVideo = isVideo;
    }

    public String getUrl() {
        return url;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl != null ? thumbnailUrl : url;
    }

    public boolean isVideo() {
        return isVideo;
    }
} 