package x.shei.db;

public class EntranceItem {
    private String title;
    private String iconResId;
    private Class<?> activityClass;

    public EntranceItem(String title, String iconResId, Class<?> activityClass) {
        this.title = title;
        this.iconResId = iconResId;
        this.activityClass = activityClass;
    }

    public String getTitle() {
        return title;
    }

    public String getIconResId() {
        return iconResId;
    }

    public Class<?> getActivityClass() {
        return activityClass;
    }
}
