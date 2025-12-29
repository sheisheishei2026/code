package x.shei.activity;

public class SoundEffect {
    private int resourceId;
    private String name;
    private String emoji;

    public SoundEffect(int resourceId, String name, String emoji) {
        this.resourceId = resourceId;
        this.name = name;
        this.emoji = emoji;
    }

    public int getResourceId() {
        return resourceId;
    }

    public String getName() {
        return name;
    }

    public String getEmoji() {
        return emoji;
    }
}