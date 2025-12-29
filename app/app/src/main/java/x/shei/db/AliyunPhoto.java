package x.shei.db;

public class AliyunPhoto {
    private String fileId;
    private String name;
    private String thumbnailUrl;
    private String downloadUrl;
    private long size;
    private String updatedAt;

    public AliyunPhoto(String fileId, String name, String thumbnailUrl, String downloadUrl, long size, String updatedAt) {
        this.fileId = fileId;
        this.name = name;
        this.thumbnailUrl = thumbnailUrl;
        this.downloadUrl = downloadUrl;
        this.size = size;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getFileId() { return fileId; }
    public String getName() { return name; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getDownloadUrl() { return downloadUrl; }
    public long getSize() { return size; }
    public String getUpdatedAt() { return updatedAt; }
}
