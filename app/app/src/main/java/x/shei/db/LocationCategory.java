package x.shei.db;

import java.util.List;

import x.shei.fragment.MapFragment;

public class LocationCategory {
    private String title;
    private List<Integer> nav;
    private List<PlaceItem> navList;
    private String ifTag;

    public LocationCategory(String title, List<Integer> nav, String ifTag) {
        this.title = title;
        this.nav = nav;
        this.ifTag = ifTag;
    }

    public String getTitle() {
        return title;
    }

    public List<Integer> getNav() {
        return nav;
    }

    public List<PlaceItem> getNavList() {
        return navList;
    }

    public void setNavList(List<PlaceItem> navList) {
        this.navList = navList;
    }

    public String getIfTag() {
        return ifTag;
    }
}


